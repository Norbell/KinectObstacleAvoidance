#include <Windows.h>
#include <Kinect.h>
#include <opencv2/opencv.hpp>
#include "DepthSensor.cpp"
#include "IRSensor.cpp"

using namespace cv;
using namespace std;

int main(int argc, CHAR* argv[]) {	

	cv::setUseOptimized(true);

	// Sensor
	IKinectSensor* pSensor;
	HRESULT hResult = S_OK;
	hResult = GetDefaultKinectSensor(&pSensor);
	if (FAILED(hResult)){
		std::cerr << "Error : GetDefaultKinectSensor" << std::endl;
		return -1;
	}

	hResult = pSensor->Open();
	if (FAILED(hResult)){
		std::cerr << "Error : IKinectSensor::Open()" << std::endl;
		return -1;
	}
	DepthSensor depthSensor = DepthSensor(pSensor);

	IRSensor irSensor = IRSensor(pSensor);

	return 0;
}
