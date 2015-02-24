#include <Windows.h>
#include <Kinect.h>
#include <fstream>  
#include <float.h>
#include <iostream>
#include <conio.h>
#include <string>

class KinectDepthSensor {
private:
	HRESULT hResult = S_OK;
	IDepthFrameSource* pDepthSource;
	IDepthFrameReader* pDepthReader;
	IFrameDescription* pDepthDescription;
	unsigned short minDepth, maxDepth;
	int depthWidth, depthHeight = 0;	
	unsigned int depthBufferSize = 0;

public:
	KinectDepthSensor::KinectDepthSensor(IKinectSensor* pSensor) {
		hResult = pSensor->get_DepthFrameSource(&pDepthSource);
		if (FAILED(hResult)){
			std::cerr << "Error : IKinectSensor::get_DepthFrameSource()" << std::endl;
			return;
		}

		hResult = pDepthSource->OpenReader(&pDepthReader);
		if (FAILED(hResult)){
			std::cerr << "Error : IDepthFrameSource::OpenReader()" << std::endl;
			return;
		}

		hResult = pDepthSource->get_FrameDescription(&pDepthDescription);
		if (FAILED(hResult)){
			std::cerr << "Error : IDepthFrameSource::get_FrameDescription()" << std::endl;
			return;
		}

		pDepthDescription->get_Width(&depthWidth); // 512
		pDepthDescription->get_Height(&depthHeight); // 424

		depthBufferSize = depthWidth * depthHeight * sizeof(unsigned short);

		pDepthSource->get_DepthMinReliableDistance(&minDepth);
		pDepthSource->get_DepthMaxReliableDistance(&maxDepth);
	}

	int KinectDepthSensor::getMinRange() {
		return minDepth;
	}

	int KinectDepthSensor::getMaxRange() {
		return maxDepth;
	}

	unsigned int KinectDepthSensor::getSensorWidth() {
		return depthWidth;
	}

	unsigned int KinectDepthSensor::getSensorHeight() {
		return depthHeight;
	}

	int KinectDepthSensor::getBufferSize() {
		return depthBufferSize;
	}

	void KinectDepthSensor::getDepthFrameSource(IDepthFrameSource** frameSource) {
		frameSource = &pDepthSource;
	}

	void KinectDepthSensor::getDepthFrameReader(IDepthFrameReader** reader) {
		reader = &pDepthReader;
	}
	
	void KinectDepthSensor::getDepthFrameDescription(IFrameDescription** description) {
		description = &pDepthDescription;
	}
};