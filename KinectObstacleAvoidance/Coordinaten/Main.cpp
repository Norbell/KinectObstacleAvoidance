#include <Windows.h>
#include <Kinect.h>
#include <fstream>  
#include <float.h>
#include <cmath>
#include <iostream>
#include <conio.h>
#include <tchar.h>
#include <string>
#include <fstream>
#include "tserial.h"
#include "bot_control.h"
#include <opencv2/opencv.hpp>


template<class Interface>
inline void SafeRelease( Interface *& pInterfaceToRelease )
{
	if( pInterfaceToRelease != NULL ){
		pInterfaceToRelease->Release();
		pInterfaceToRelease = NULL;
	}
}

int main( int argc, CHAR* argv[] )
{
	cv::setUseOptimized(true);
	HRESULT hResult = S_OK;
	
	//color values
	cv::Scalar yellow	= cv::Scalar(255, 0, 255);
	cv::Scalar red		= cv::Scalar(255, 255, 255);

	unsigned int frameCounter = 1;
	unsigned int interval = 5;
	unsigned int frameInterval = 0;
	unsigned int useablePixels = 0;		//counts the useable pixels per frame
	unsigned int frameErrors = 0;		//Counts the number of errors per frame

	struct CameraSpaces {
		CameraSpacePoint cameraPoint;
		DepthSpacePoint depthPoint;
		ColorSpacePoint colorPoint;
	};

	CameraSpaces rightSpace;
	CameraSpaces leftSpace;
	CameraSpaces centerSpace;

	rightSpace.cameraPoint = { 8, 8, 8 }; //8m is the max measurable z value
	leftSpace.cameraPoint = { 8, 8, 8 }; //8m is the max measurable z value
	centerSpace.cameraPoint = { 8, 8, 8 }; //8m is the max measurable z value

	IKinectSensor* pSensor;
	ICoordinateMapper* pCoordinateMapper;

	/* Kinect-Sensor */
		hResult = GetDefaultKinectSensor( &pSensor );
		if( FAILED( hResult ) ){
			std::cerr << "Error : GetDefaultKinectSensor" << std::endl;
			return -1;
		}

		hResult = pSensor->Open();
		if( FAILED( hResult ) ){
			std::cerr << "Error : IKinectSensor::Open()" << std::endl;
			return -1;
		}
	/***********************************************************/


	/* Color-Sensor */
		IColorFrameSource* pColorSource;
		IColorFrameReader* pColorReader;
		IFrameDescription* pColorDescription;
		int colorWidth, colorHeight = 0;
		unsigned int colorBufferSize;

		hResult = pSensor->get_ColorFrameSource(&pColorSource);
		if (FAILED(hResult)){
			std::cerr << "Error : IKinectSensor::get_ColorFrameSource()" << std::endl;
			return -1;
		}

		hResult = pColorSource->OpenReader(&pColorReader);
		if (FAILED(hResult)){
			std::cerr << "Error : IColorFrameSource::OpenReader()" << std::endl;
			return -1;
		}

		hResult = pColorSource->get_FrameDescription(&pColorDescription);
		if (FAILED(hResult)){
			std::cerr << "Error : IColorFrameSource::get_FrameDescription()" << std::endl;
			return -1;
		}

		pColorDescription->get_Width(&colorWidth); // 1920
		pColorDescription->get_Height(&colorHeight); // 1080

		colorBufferSize = colorWidth * colorHeight * 4 * sizeof(unsigned char);
	/***********************************************************/


	/* Depth-Sensor */
		IDepthFrameSource* pDepthSource;
		IDepthFrameReader* pDepthReader;
		IFrameDescription* pDepthDescription;
		unsigned short minDepth, maxDepth;
		int depthWidth, depthHeight = 0;
		unsigned int depthBufferSize = 0;

		hResult = pSensor->get_DepthFrameSource(&pDepthSource);
		if (FAILED(hResult)){
			std::cerr << "Error : IKinectSensor::get_DepthFrameSource()" << std::endl;
			return -1;
		}

		hResult = pDepthSource->OpenReader(&pDepthReader);
		if (FAILED(hResult)){
			std::cerr << "Error : IDepthFrameSource::OpenReader()" << std::endl;
			return -1;
		}

		hResult = pDepthSource->get_FrameDescription(&pDepthDescription);
		if (FAILED(hResult)){
			std::cerr << "Error : IDepthFrameSource::get_FrameDescription()" << std::endl;
			return -1;
		}

		pDepthDescription->get_Width(&depthWidth); // 512
		pDepthDescription->get_Height(&depthHeight); // 424

		depthBufferSize = depthWidth * depthHeight * sizeof(unsigned short);

		pDepthSource->get_DepthMinReliableDistance(&minDepth);
		pDepthSource->get_DepthMaxReliableDistance(&maxDepth);
	/***********************************************************/


	/* IR-Sensor */
		IInfraredFrameSource* pInfraredSource;
		IInfraredFrameReader* pInfraredReader;
		IFrameDescription* pDescription;
		int irWidth, irHeight = 0;

		hResult = pSensor->get_InfraredFrameSource(&pInfraredSource);
		if (FAILED(hResult)){
			std::cerr << "Error : IKinectSensor::get_InfraredFrameSource()" << std::endl;
			return -1;
		}

		hResult = pInfraredSource->OpenReader(&pInfraredReader);
		if (FAILED(hResult)){
			std::cerr << "Error : IInfraredFrameSource::OpenReader()" << std::endl;
			return -1;
		}

		hResult = pInfraredSource->get_FrameDescription(&pDescription);
		if (FAILED(hResult)){
			std::cerr << "Error : IInfraredFrameSource::get_FrameDescription()" << std::endl;
			return -1;
		}

		pDescription->get_Width(&irWidth); // 512
		pDescription->get_Height(&irHeight); // 424
	/***********************************************************/


	/* Coordinate Mapper */
		hResult = pSensor->get_CoordinateMapper(&pCoordinateMapper);
		if (FAILED(hResult)){
			std::cerr << "Error : IKinectSensor::get_CoordinateMapper()" << std::endl;
			return -1;
		}	
	/***********************************************************/


	/* Create Windows */
		cv::Mat colorBufferMat(colorHeight, colorWidth , CV_8UC4);
		cv::Mat colorMat( colorHeight / 2, colorWidth / 2, CV_8UC4 );
		cv::namedWindow( "Color" );

		cv::Mat depthBufferMat( depthHeight, depthWidth, CV_16UC1 );
		cv::Mat depthMat( depthHeight, depthWidth, CV_8UC1 );
		cv::namedWindow( "Depth" );

		cv::Mat infraredMat(depthHeight, depthWidth, CV_8UC1);
		cv::namedWindow("Infrared");
	/***********************************************************/


	while (1){
		std::fstream fs;
		fs.open("KinectSensorLog.txt", std::fstream::in | std::fstream::out | std::fstream::app);
		
		// Color Frame
		IColorFrame* pColorFrame = nullptr;
		hResult = pColorReader->AcquireLatestFrame(&pColorFrame);
		if (SUCCEEDED(hResult)){
			hResult = pColorFrame->CopyConvertedFrameDataToArray(colorBufferSize, reinterpret_cast<BYTE*>(colorBufferMat.data), ColorImageFormat::ColorImageFormat_Bgra);
			if (SUCCEEDED(hResult)){
				cv::resize(colorBufferMat, colorMat, cv::Size(), 0.5, 0.5);
			}
		} //SafeRelease( pColorFrame );


		// Infrared Frame 
		IInfraredFrame* pInfraredFrame = nullptr;
		hResult = pInfraredReader->AcquireLatestFrame(&pInfraredFrame);
		if (SUCCEEDED(hResult)){
			unsigned int bufferSize = 0;
			unsigned short* infraredbuffer = nullptr;
			hResult = pInfraredFrame->AccessUnderlyingBuffer(&bufferSize, &infraredbuffer);
			if (SUCCEEDED(hResult)){
				for (int y = 0; y < irHeight; y++){
					for (int x = 0; x < irWidth; x++){
						unsigned int index = y * irWidth + x;
						infraredMat.at<unsigned char>(y, x) = infraredbuffer[index] >> 7;
					}
				}
			}
		} //SafeRelease( pInfraredFrame );


		// Depth Frame
		IDepthFrame* pDepthFrame = nullptr;
		hResult = pDepthReader->AcquireLatestFrame(&pDepthFrame);
		if (SUCCEEDED(hResult)){
			hResult = pDepthFrame->AccessUnderlyingBuffer(&depthBufferSize, reinterpret_cast<UINT16**>(&depthBufferMat.data));
			if (SUCCEEDED(hResult)){
				depthBufferMat.convertTo(depthMat, CV_8U, -255.0f / 8000.0f, 255.0f);
			}
		} //SafeRelease( pDepthFrame );


		if (SUCCEEDED(hResult)) {
			if (frameCounter == 1) {
				fs << "/***** General informations *****/" << std::endl;
				fs << "Sensor Resolution -->	" << "WxH: " << depthWidth << "x" << depthHeight << std::endl;
				fs << "Sensor Min and Max -->	" << "min: " << minDepth << " max: " << maxDepth << "\r\n";
			}

			std::vector<CameraSpacePoint> cameraSpacePoints(depthWidth * depthHeight);
			hResult = pCoordinateMapper->MapDepthFrameToCameraSpace(depthWidth * depthHeight, reinterpret_cast<UINT16*>(depthBufferMat.data), depthWidth * depthHeight, &cameraSpacePoints[0]);
			if (SUCCEEDED(hResult)) {
				//LOG(INFO) << "/***** Frame - " << imageCounter << " *****/" << std::endl;
				//LOG(INFO) << "Height y: " << depthHeight << " Width x: " << depthWidth << std::endl;				

				for (int i = 0; i < cameraSpacePoints.size(); i++) {
					CameraSpacePoint point = cameraSpacePoints[i];

					//fs << "PixelNr: " << i << std::endl;

					if (_fpclass(point.Z) == 4) {
						//std::cout << "Infinit" << std::endl;
						//LOG(INFO) << "Infinit";
						frameErrors++;
					}
					/*else if (point.Z > farthest.Z) {
						farthest = point;
						useablePixels++;
						//LOG(INFO) << "Farthest -->  x: " << x << " y: " << y << " Px: " << point.X << " Py: " << point.Y << " Pz: " << point.Z;
						fs << "Farthest --> Px: " << point.X << " Py: " << point.Y << " Pz: " << point.Z << std::endl;
						fs << "Depth-Farthest --> x: " << depthPoint.X << " y: " << depthPoint.Y << "\r\n";
						}*/
					else if (point.Z < centerSpace.cameraPoint.Z) {
						useablePixels++;
						centerSpace.cameraPoint = point;

						fs << "1 Meter --> Px: " << point.X << " Py: " << point.Y << " Pz: " << point.Z << std::endl;
						//fs << "Depth-Nearest --> x: " << depthPoint.X << " y: " << depthPoint.Y << "\r\n";
					}
					else {
						useablePixels++;
						//std::cout << "Something else" << std::endl;
					}					
				}// End of for(sizeof CameraSpacePoints-Vector)				

				fs << "/***** Values per Frame *****/" << std::endl;
				fs << "Frame:	" << frameCounter << std::endl;
				fs << "Errors (Not measurable):	" << frameErrors << std::endl;
				fs << "Useable Pixels:	" << useablePixels << std::endl;
				float procent = (float)useablePixels / (float)(depthWidth * depthHeight) * 100;
				fs << "Useable Pixels(%):	" << procent << "\r\n";

				frameCounter++;
				useablePixels = 0;
			} //End of if

			hResult = pCoordinateMapper->MapCameraPointToDepthSpace(centerSpace.cameraPoint, &centerSpace.depthPoint);
			if (SUCCEEDED(hResult)) {
				hResult = pCoordinateMapper->MapCameraPointToColorSpace(centerSpace.cameraPoint, &centerSpace.colorPoint);
				if (SUCCEEDED(hResult)) {
					cv::Point nCenterPoint = cv::Point(centerSpace.depthPoint.X, centerSpace.depthPoint.Y);

					//cv::line(nearestPointMat, FP, centerPoint, farYellow, 3, 8, 0);
					//cv::circle(nearestPointMat, p1, 3, red, 3, 8, 0);
					
					cv::circle(infraredMat, nCenterPoint, 8, red, 4, 8, 0);

					fs << "CENTER Depth --> " << "X: " << centerSpace.depthPoint.X << " Y: " << centerSpace.depthPoint.Y << " Z: " << centerSpace.cameraPoint.Z << "\r\n";
					//fs << "CENTER Color --> " << "X: " << centerSpace.colorPoint.X << " Y: " << centerSpace.colorPoint.Y << " Z: " << centerSpace.cameraPoint.Z << "\r\n";

				}//End of if(hresult of ColorSpacePoint)
			}//End of if(hresult of DepthSpacePoint)

			fs.close();
		}//End of if

				
		SafeRelease( pColorFrame );
		SafeRelease(pInfraredFrame);
		SafeRelease( pDepthFrame );

		cv::imshow("Infrared", infraredMat);
		cv::imshow( "Color", colorMat );
		cv::imshow( "Depth", depthMat );

		if( cv::waitKey( 30 ) == VK_ESCAPE ){
			break;
		}
	}

	SafeRelease( pColorSource );
	SafeRelease( pDepthSource );
	SafeRelease( pColorReader );
	SafeRelease( pDepthReader );
	SafeRelease( pColorDescription );
	SafeRelease( pDepthDescription );
	SafeRelease( pCoordinateMapper );
	if( pSensor ){
		pSensor->Close();
	}
	SafeRelease( pSensor );
	cv::destroyAllWindows();

	return 0;
}