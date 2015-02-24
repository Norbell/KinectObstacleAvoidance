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

class KinectIRSensor {
private:
	HRESULT hResult = S_OK;
	IInfraredFrameSource* pInfraredSource;
	IInfraredFrameReader* pInfraredReader;
	IFrameDescription* pDescription;
	int irWidth, irHeight = 0;

public:
	KinectIRSensor::KinectIRSensor(IKinectSensor* pSensor) {
		hResult = pSensor->get_InfraredFrameSource(&pInfraredSource);
		if (FAILED(hResult)){
			std::cerr << "Error : IKinectSensor::get_InfraredFrameSource()" << std::endl;
			return;
		}

		hResult = pInfraredSource->OpenReader(&pInfraredReader);
		if (FAILED(hResult)){
			std::cerr << "Error : IInfraredFrameSource::OpenReader()" << std::endl;
			return;
		}

		hResult = pInfraredSource->get_FrameDescription(&pDescription);
		if (FAILED(hResult)){
			std::cerr << "Error : IInfraredFrameSource::get_FrameDescription()" << std::endl;
			return;
		}

		pDescription->get_Width(&irWidth); // 512
		pDescription->get_Height(&irHeight); // 424
	}

	int KinectIRSensor::getSensorWidth() {
		return irWidth;
	}

	int KinectIRSensor::getSensorHeight() {
		return irHeight;
	}

	void KinectIRSensor::getInfraredFrameSource(IInfraredFrameSource** frameSource) {
		frameSource = &pInfraredSource;
	}

	void KinectIRSensor::getIInfraredFrameReader(IInfraredFrameReader** reader) {
		reader = &pInfraredReader;
	}

	void KinectIRSensor::getIFrameDescription(IFrameDescription** description) {
		description = &pDescription;
	}
};