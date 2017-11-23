#!/bin/bash
set +x
 
collection=postman.json
file=list.txt
 
if [ -f $file ] ; then
    rm $file
fi
 
### Mount unmount test case
devices=("mount_unmount_env.json" "mount_unmount_telnet_env.json" "mount_unmount_ios1553_env.json")
folders=("Mount/Unmount IOS")
 
for device in ${devices[@]}
do
   echo Collection running with $device
     for folder in "${folders[@]}"
     do 
        newman run $collection --bail -e $device -n 2 --folder "$folder"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $device testing $folder FAILED" >> $file; fi
        sleep 5
     done
done
 
### Test for IOS XR
XR_devices=("xrv_env.json" "asr_env.json")
XR_folders=("Mount" "General information" "Interface" "Interface IP" "ospf" "static route" "BGP summary" "CDP" "LLDP" "Unmount")
ASR_folders=("Mount" "General information" "Interface" "Interface IP" "ospf" "static route" "BGP summary" "Platform" "CDP" "LLDP" "Unmount")
 
for device in ${XR_devices[@]}
do
   echo Collection running with $device
         if [ "$device" == "xrv_env.json" ]
         then 
             for folder in "${XR_folders[@]}"
             do   
                newman run $collection --bail -e $device -n 1 --folder "XR $folder"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $device testing XR $folder FAILED" >> $file; fi
                sleep 5
             done
         fi
         
         if [ "$device" == "asr_env.json" ]
         then 
             for folder in "${ASR_folders[@]}"
             do   
                newman run $collection --bail -e $device -n 1 --folder "XR $folder"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $device testing XR $folder FAILED" >> $file; fi
                sleep 5
             done
         fi 
done
 
### Test for IOS
IOS_devices=("classic_152_env.json" "classic_1553_env.json" "xe_env.json")
Classic_folders=("Mount" "General information" "Interface" "Interface IP" "ospf/vrf" "static route" "BGP summary" "journal/dry-run" "CDP" "Unmount")
XE_folders=("Mount" "General information" "Interface" "Interface IP" "ospf/vrf" "static route" "BGP summary" "journal/dry-run" "CDP" "LLDP" "Unmount")

for device in ${IOS_devices[@]}
do
   echo Collection running with $device
         if [ "$device" == "classic_152_env.json" ]
         then 
             for folder in "${Classic_folders[@]}"
             do   
                newman run $collection --bail -e $device -n 1 --folder "Classic $folder"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $device testing Classic $folder FAILED" >> $file; fi
                sleep 5
             done
         fi
         if [ "$device" == "classic_1553_env.json" ]
         then 
             for folder in "${Classic_folders[@]}"
             do   
                newman run $collection --bail -e $device -n 1 --folder "Classic $folder"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $device testing Classic $folder FAILED" >> $file; fi
                sleep 5
             done
         fi         
         if [ "$device" == "xe_env.json" ]
         then 
             for folder in "${XE_folders[@]}"
             do   
                newman run $collection --bail -e $device -n 1 --folder "Classic $folder"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $device testing Classic $folder FAILED" >> $file; fi
                sleep 5
             done
         fi 
done
 
### Test for Linux generic
Linux_devices=("linux_157_env.json")
folders=("Linux")
 
for device in "${Linux_devices[@]}"
do
   echo Collection running with $device
     for folder in "${folders[@]}"
     do 
        newman run $collection --bail -e $device -n 1 --folder "Linux"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $device testing $folder FAILED" >> $file; fi
        sleep 5
     done
done
 
 
if [ -f $file ] ; then
    cat $file
    rm $file
fi
