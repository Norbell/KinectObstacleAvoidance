using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using Microsoft.Kinect;

namespace Kinect
{
    /// <summary>
    /// Interaction logic for KinectWindow.xaml
    /// </summary>
    public partial class KinectWindow : Window
    {
        const float MaxDepthDistance = 4000; // max value returned
        const float MinDepthDistance = 850; // min value returned
        const float MaxDepthDistanceOffset = MaxDepthDistance - MinDepthDistance;
        int framecount = 0;
        
        public KinectWindow()
        {
            InitializeComponent();
        }
        KinectSensor Sensor;
        private void Window_Loaded(object sender, RoutedEventArgs e)
        {
            if (KinectSensor.KinectSensors.Count > 0)
            {
                Sensor = KinectSensor.KinectSensors[0];
                if (Sensor.Status == KinectStatus.Connected)
                {
                    Sensor.DepthStream.Enable(DepthImageFormat.Resolution80x60Fps30);
                    //Sensor.ColorStream.Enable();
                    //Sensor.SkeletonStream.Enable();
                    Sensor.AllFramesReady += new EventHandler<AllFramesReadyEventArgs>(Sensor_AllFramesReady);
                    Sensor.Start();
                }
            }

        }


        void Sensor_AllFramesReady(object sender, AllFramesReadyEventArgs e)
        {
            using (DepthImageFrame DepthFrame = e.OpenDepthImageFrame())
            {
                if (DepthFrame == null)
                {
                    return;
                }
                byte[] pixels = GenerateColoredBytes(DepthFrame);
                int[] deptharray = DetectCollision(DepthFrame);
                int section0count = 0;
                int section1count = 0;
                int section2count = 0;
                int section3count = 0;
                int section4count = 0;
                int count = 0;
                const int Section0 = 15;
                const int Section1 = 31;
                const int Section2 = 47;
                const int Section3 = 63;
                const int Section4 = 79;
                foreach (int depth in deptharray)
                {
                    if ((depth <= DepthFrame.MinDepth) && (count <= Section0))
                    {
                        section0count++;
                    }
                    else if ((depth <= DepthFrame.MinDepth) && (count <= Section1))
                    {
                        section1count++;
                    }
                    else if ((depth <= DepthFrame.MinDepth) && (count <= Section2))
                    {
                        section2count++;
                    }
                    else if ((depth <= DepthFrame.MinDepth) && (count <= Section3))
                    {
                        section3count++;
                    }
                    else if ((depth <= DepthFrame.MinDepth) && (count <= Section4))
                    {
                        section4count++;
                    }
                    count++;
                    if (count == 80)
                    {
                        count = 0;
                    }
                    if (section0count == 3840)
                    {
                        Section.Content = "0";
                    }
                    else if (section1count == 3840)
                    {
                        Section.Content = "1";
                    }
                    else if (section2count == 3840)
                    {
                        Section.Content = "2";
                    }
                    else if (section3count == 3840)
                    {
                        Section.Content = "3";
                    }
                    else if (section4count == 3840)
                    {
                        Section.Content = "4";
                    }
                    else
                    {
                       Section.Content = "-";
                    }
                }

                //framecount++;
                int stride = DepthFrame.Width * 4;
                image1.Source = BitmapSource.Create(DepthFrame.Width, DepthFrame.Height, 96,96, PixelFormats.Bgr32, null, pixels, stride);
                //if (framecount == 90)
                //{
                 //   framecount = 0;
                 //   MessageBox.Show(section0count.ToString() + " " + section1count.ToString() + " " + section2count.ToString() + " " + section3count.ToString() + " " + section4count.ToString());
//}
            }
        }

        private byte[] GenerateColoredBytes(DepthImageFrame DepthFrame)
        {
            short[] rawDepthData = new short[DepthFrame.PixelDataLength];
            DepthFrame.CopyPixelDataTo(rawDepthData);

            Byte[] pixels = new byte[DepthFrame.Height * DepthFrame.Width * 4];
            int[] deptharray = new int[DepthFrame.Height * DepthFrame.Width * 4];

            const int BlueIndex = 0;
            const int GreenIndex = 1;
            const int RedIndex = 2;

            for (int depthindex = 0, colorIndex = 0; depthindex < rawDepthData.Length && colorIndex < pixels.Length; depthindex++, colorIndex += 4)
            {
                //get the player (requires Skeleton tracking enabled for values)
                int player = rawDepthData[depthindex] & DepthImageFrame.PlayerIndexBitmask;

                //gets the depth value
                int depth = rawDepthData[depthindex] >> DepthImageFrame.PlayerIndexBitmaskWidth;

                byte intensity = CalculateIntensityFromDepth(depth);
                pixels[colorIndex + BlueIndex] = intensity;
                pixels[colorIndex + GreenIndex] = intensity;
                pixels[colorIndex + RedIndex] = intensity;
            }

            return pixels;
        }
        void StopKinect(KinectSensor Sensor)
        {
            if (Sensor != null)
            {
                Sensor.Stop();
                Sensor.AudioSource.Stop();
            }
        }

        private int[] DetectCollision(DepthImageFrame DepthFrame)
        {
            short[] rawDepthData = new short[DepthFrame.PixelDataLength];
            DepthFrame.CopyPixelDataTo(rawDepthData);
            int[] DepthArray = new int[DepthFrame.Height * DepthFrame.Width * 4];
            Byte[] pixels = new byte[DepthFrame.Height * DepthFrame.Width * 4];
            for (int depthindex = 0, colorIndex = 0; depthindex < rawDepthData.Length && colorIndex < pixels.Length; depthindex++, colorIndex += 4)
            {
                //get the player (requires Skeleton tracking enabled for values)
                //int player = rawDepthData[depthindex] & DepthImageFrame.PlayerIndexBitmask;

                //gets the depth value
                int depth = rawDepthData[depthindex] >> DepthImageFrame.PlayerIndexBitmaskWidth;
                DepthArray[depthindex] = depth;
            }                                   
                
            return DepthArray;
        }

        public static byte CalculateIntensityFromDepth(int Distance)
        {
            return (byte)(255-(255*Math.Max(Distance - MinDepthDistance, 0) / MaxDepthDistanceOffset));
        }

        void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            StopKinect(Sensor);
        }

        private void button1_Click(object sender, RoutedEventArgs e)
        {
            Sensor.ElevationAngle = 17;
        }


    } 
}
