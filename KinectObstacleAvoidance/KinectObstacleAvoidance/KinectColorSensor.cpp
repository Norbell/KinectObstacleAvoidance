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

class KinectColorSensor {
private:
	HRESULT hResult = S_OK;
	IColorFrameSource* pColorSource;
	IColorFrameReader* pColorReader;
	IFrameDescription* pColorDescription;
	int colorWidth, colorHeight = 0;
	unsigned int colorBufferSize;

public:
	KinectColorSensor::KinectColorSensor(IKinectSensor* pSensor) {
		hResult = pSensor->get_ColorFrameSource(&pColorSource);
		if (FAILED(hResult)){
			std::cerr << "Error : IKinectSensor::get_ColorFrameSource()" << std::endl;
			return;
		}

		hResult = pColorSource->OpenReader(&pColorReader);
		if (FAILED(hResult)){
			std::cerr << "Error : IColorFrameSource::OpenReader()" << std::endl;
			return;
		}

		hResult = pColorSource->get_FrameDescription(&pColorDescription);
		if (FAILED(hResult)){
			std::cerr << "Error : IColorFrameSource::get_FrameDescription()" << std::endl;
			return;
		}

		pColorDescription->get_Width(&colorWidth); // 1920
		pColorDescription->get_Height(&colorHeight); // 1080

		colorBufferSize = colorWidth * colorHeight * 4 * sizeof(unsigned char);
	}

	int KinectColorSensor::getSensorWidth() {
		return colorWidth;
	}

	int KinectColorSensor::getSensorHeight() {
		return colorHeight;
	}

	unsigned int KinectColorSensor::getBufferSize() {
		return colorBufferSize;
	}

	void KinectColorSensor::getColorFrameSourc(IColorFrameSource** frameSource) {
		frameSource = &pColorSource;
	}

	void KinectColorSensor::getColorFrameReader(IColorFrameReader** reader) {
		reader = &pColorReader;
	}

	void KinectColorSensor::getColorFrameDescription(IFrameDescription** description) {
		description = &pColorDescription;
	}
};