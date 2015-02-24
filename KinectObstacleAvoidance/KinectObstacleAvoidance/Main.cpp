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

#include "KinectColorSensor.cpp"
#include "KinectDepthSensor.cpp"
#include "KinectIRSensor.cpp"


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
	bool showCoordinateMappingWindow = false;
	
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
	KinectColorSensor* ColorSensor;
	KinectDepthSensor* DepthSensor;
	KinectIRSensor* IRSensor;
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
		ColorSensor = new KinectColorSensor(pSensor);
		
		IColorFrameSource* pColorSource;
		ColorSensor->getColorFrameSourc(&pColorSource);

		IColorFrameReader* pColorReader;
		ColorSensor->getColorFrameReader(&pColorReader);

		IFrameDescription* pColorDescription;
		ColorSensor->getColorFrameDescription(&pColorDescription);

		int colorWidth	= ColorSensor->getSensorWidth();
		int colorHeight	= ColorSensor->getSensorHeight();
	/***********************************************************/


	/* Depth-Sensor */
		DepthSensor	= new KinectDepthSensor(pSensor);

		IDepthFrameSource* pDepthSource;
		DepthSensor->getDepthFrameSource(&pDepthSource);

		IDepthFrameReader* pDepthReader;
		DepthSensor->getDepthFrameReader(&pDepthReader);

		IFrameDescription* pDepthDescription;
		DepthSensor->getDepthFrameDescription(&pDepthDescription);

		int depthWidth	= DepthSensor->getSensorWidth();
		int depthHeight = DepthSensor->getSensorHeight();
		unsigned int minDepth	= DepthSensor->getMinRange();
		unsigned int maxDepth	= DepthSensor->getMaxRange();
	/***********************************************************/


	/* IR-Sensor */
		IRSensor = new KinectIRSensor(pSensor);

		IInfraredFrameSource* pInfraredSource;
		IRSensor->getInfraredFrameSource(&pInfraredSource);

		IInfraredFrameReader* pInfraredReader;
		IRSensor->getIInfraredFrameReader(&pInfraredReader);

		IFrameDescription* pInfraredDescription;
		IRSensor->getIFrameDescription(&pInfraredDescription);
	/***********************************************************/


	/* Coordinate Mapper */
		hResult = pSensor->get_CoordinateMapper(&pCoordinateMapper);
		if (FAILED(hResult)){
			std::cerr << "Error : IKinectSensor::get_CoordinateMapper()" << std::endl;
			return -1;
		}	
	/***********************************************************/


	/* Create Windows */
		cv::Mat infraredMat(IRSensor->getSensorWidth(), IRSensor->getSensorHeight(), CV_8UC1);
		cv::namedWindow("Infrared");

		cv::Mat colorBufferMat( ColorSensor->getSensorWidth(), ColorSensor->getSensorHeight(), CV_8UC4 );
		cv::Mat colorMat( ColorSensor->getSensorHeight() / 2, ColorSensor->getSensorWidth() / 2, CV_8UC4 );
		cv::namedWindow( "Color" );

		cv::Mat depthBufferMat( DepthSensor->getSensorHeight(), DepthSensor->getSensorWidth(), CV_16UC1 );
		cv::Mat depthMat( DepthSensor->getSensorHeight() , DepthSensor->getSensorWidth(), CV_8UC1 );
		cv::namedWindow( "Depth" );

		cv::Mat coordinateMapperMat(DepthSensor->getSensorHeight(), DepthSensor->getSensorWidth(), CV_8UC4);
		if (showCoordinateMappingWindow) {
			cv::namedWindow("CoordinateMapper");
		}
	/***********************************************************/


	while (1){
		std::fstream fs;
		fs.open("newlog.txt", std::fstream::in | std::fstream::out | std::fstream::app);
		
		// Color Frame
		IColorFrame* pColorFrame = nullptr;
		hResult = pColorReader->AcquireLatestFrame(&pColorFrame);
		if (SUCCEEDED(hResult)){
			hResult = pColorFrame->CopyConvertedFrameDataToArray(ColorSensor->getBufferSize(), reinterpret_cast<BYTE*>(colorBufferMat.data), ColorImageFormat::ColorImageFormat_Bgra);
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
				for (int y = 0; y < IRSensor->getSensorHeight() ; y++){
					for (int x = 0; x < IRSensor->getSensorWidth() ; x++){
						unsigned int index = y * IRSensor->getSensorWidth() + x;
						infraredMat.at<unsigned char>(y, x) = infraredbuffer[index] >> 7;
					}
				}
			}
		} //SafeRelease( pInfraredFrame );


		// Depth Frame
		IDepthFrame* pDepthFrame = nullptr;
		unsigned int depthBufferSize = DepthSensor->getBufferSize();
		hResult = pDepthReader->AcquireLatestFrame(&pDepthFrame);
		if (SUCCEEDED(hResult)){
			hResult = pDepthFrame->AccessUnderlyingBuffer(&depthBufferSize, reinterpret_cast<UINT16**>(&depthBufferMat.data));
			if (SUCCEEDED(hResult)){
				depthBufferMat.convertTo(depthMat, CV_8U, -255.0f / 8000.0f, 255.0f);
			}
		} //SafeRelease( pDepthFrame );

		if (showCoordinateMappingWindow){
			if (SUCCEEDED(hResult)){
				std::vector<ColorSpacePoint> colorSpacePoints(depthWidth * depthHeight);
				hResult = pCoordinateMapper->MapDepthFrameToColorSpace(depthWidth * depthHeight, reinterpret_cast<UINT16*>(depthBufferMat.data), depthWidth * depthHeight, &colorSpacePoints[0]);
				if (SUCCEEDED(hResult)){
					coordinateMapperMat = cv::Scalar(0, 0, 0, 0);
					for (int y = 0; y < depthHeight; y++){
						for (int x = 0; x < depthWidth; x++){
							unsigned int index = y * depthWidth + x;
							ColorSpacePoint point = colorSpacePoints[index];
							int colorX = static_cast<int>(std::floor(point.X + 0.5));
							int colorY = static_cast<int>(std::floor(point.Y + 0.5));
							unsigned short depth = depthBufferMat.at<unsigned short>(y, x);
							if ((colorX >= 0) && (colorX < ColorSensor->getSensorWidth()) && (colorY >= 0) && (colorY < ColorSensor->getSensorHeight())/* && ( depth >= minDepth ) && ( depth <= maxDepth )*/){
								coordinateMapperMat.at<cv::Vec4b>(y, x) = colorBufferMat.at<cv::Vec4b>(colorY, colorX);
							}
						} //End of for(x)
					} //End of for(y)
				} //End of if
			}
		}


		if (SUCCEEDED(hResult)) {
			if (frameCounter == 1) {
				fs << "/***** General informations *****/" << std::endl;
				fs << "Sensor Resolution -->	" << "WxH: " << depthWidth << "x" << depthHeight << std::endl;
				fs << "Sensor Min and Max -->	" << "min: " << DepthSensor->getMinRange() << " max: " << DepthSensor->getMaxRange() << "\r\n";
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

						//fs << "1 Meter --> Px: " << point.X << " Py: " << point.Y << " Pz: " << point.Z << std::endl;
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
				//float procent = (float)useablePixels / (float)sensorSize * 100;
				//fs << "Useable Pixels(%):	" << procent << "\r\n";

				frameCounter++;
				useablePixels = 0;
			} //End of if


			if (centerSpace.cameraPoint.Z <= 1.2){
			}

			hResult = pCoordinateMapper->MapCameraPointToDepthSpace(centerSpace.cameraPoint, &centerSpace.depthPoint);
			if (SUCCEEDED(hResult)) {
				hResult = pCoordinateMapper->MapCameraPointToColorSpace(centerSpace.cameraPoint, &centerSpace.colorPoint);
				if (SUCCEEDED(hResult)) {
					cv::Point nCenterPoint = cv::Point(centerSpace.depthPoint.X, centerSpace.depthPoint.Y);
					//cv::Point p2 = cv::Point(fPoint.X, fPoint.Y);

					//cv::line(nearestPointMat, FP, centerPoint, farYellow, 3, 8, 0);
					cv::Point p1 = cv::Point(0,0);
					//cv::circle(nearestPointMat, p1, 3, red, 3, 8, 0);
					
					//cv::line(infraredMat, p1, nCenterPoint, red, 3, 8, 0);
					cv::circle(infraredMat, nCenterPoint, 8, red, 4, 8, 0);
					//cv::circle(colorBufferMat, nCenterPoint, 8, red, 3, 8, 0);

					fs << "CENTER Depth --> " << "X: " << centerSpace.depthPoint.X << " Y: " << centerSpace.depthPoint.Y << " Z: " << centerSpace.cameraPoint.Z << "\r\n";
					fs << "CENTER Color --> " << "X: " << centerSpace.colorPoint.X << " Y: " << centerSpace.colorPoint.Y << " Z: " << centerSpace.cameraPoint.Z << "\r\n";

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

		if (showCoordinateMappingWindow){
			cv::imshow("CoordinateMapper", coordinateMapperMat);
		}

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

