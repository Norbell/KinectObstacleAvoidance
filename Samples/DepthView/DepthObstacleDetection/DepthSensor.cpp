#include "stdafx.h"
#include <Windows.h>
#include <opencv2/opencv.hpp>
#include <Kinect.h>

using namespace cv;
using namespace std;

class DepthSensor {

	private:
		DepthSensor::IKinectSensor* pSensor;
		DepthSensor::HRESULT hResult = S_OK;

	public:
		DepthSensor::DepthSensor(IKinectSensor  *sensor) {
			DepthSensor::pSensor = sensor;

			// Source
			IDepthFrameSource* pDepthSource;
			hResult = pSensor->get_DepthFrameSource(&pDepthSource);
			if (FAILED(hResult)){
				std::cerr << "Error : IKinectSensor::get_DepthFrameSource()" << std::endl;
				//return -1;
			}

			// Reader
			IDepthFrameReader* pDepthReader;
			hResult = pDepthSource->OpenReader(&pDepthReader);
			if (FAILED(hResult)){
				std::cerr << "Error : IDepthFrameSource::OpenReader()" << std::endl;
				//return -1;
			}

			// Description
			IFrameDescription* pDescription;
			hResult = pDepthSource->get_FrameDescription(&pDescription);
			if (FAILED(hResult)){
				std::cerr << "Error : IDepthFrameSource::get_FrameDescription()" << std::endl;
				//return -1;
			}

			int width = 0;
			int height = 0;
			pDescription->get_Width(&width); // 512
			pDescription->get_Height(&height); // 424
			unsigned int bufferSize = width * height * sizeof(unsigned short);
			std::cout << "Window : " << width << " - " << height << std::endl;


			// Range ( Range of Depth is 500-8000[mm], Range of Detection is 500-4500[mm] ) 
			unsigned short min = 0;
			unsigned short max = 0;
			pDepthSource->get_DepthMinReliableDistance(&min); // 500
			pDepthSource->get_DepthMaxReliableDistance(&max); // 4500
			std::cout << "Range : " << min << " - " << max << std::endl;
			

			cv::Mat bufferMat(height, width, CV_16UC1);
			cv::Mat depthMat(height, width, CV_8UC1);
			cv::namedWindow("Depth");

			while (1){
				// Frame
				IDepthFrame* pDepthFrame = nullptr;
				hResult = pDepthReader->AcquireLatestFrame(&pDepthFrame);
				if (SUCCEEDED(hResult)){
					hResult = pDepthFrame->AccessUnderlyingBuffer(&bufferSize, reinterpret_cast<UINT16**>(&bufferMat.data));
					if (SUCCEEDED(hResult)){
						bufferMat.convertTo(depthMat, CV_8U, -255.0f / 8000.0f, 255.0f);
					}
				}
				SafeRelease(pDepthFrame);

				cv::imshow("Depth", depthMat);

				if (cv::waitKey(30) == VK_ESCAPE){
					break;
				}
			}
			
			SafeRelease(pDepthSource);
			SafeRelease(pDepthReader);
			SafeRelease(pDescription);
			if (pSensor){
				pSensor->Close();
			}
			SafeRelease(pSensor);
			cv::destroyAllWindows();
		}
};