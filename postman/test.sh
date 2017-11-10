#!/bin/bash
set +x

collection=postman.json
folder1="Mount"
folder2="General information"
folder3="Interface"
folder4="Interface IP"
folder5="ospf"
folder6="static route"
folder7="BGP summary"
folder8="Platform"
folder9="Unmount"
folder10="ospf/vrf"
folder11="Mount/Unmount IOS"
folder12="journal/dry-run"
file=list.txt

if [ -f $file ] ; then
    rm $file
fi

### Mount unmount test case
arr=("mount_unmount_env.json" "mount_unmount_telnet_env.json" "mount_unmount_ios1553_env.json")

for i in ${arr[@]}
do
   echo Collection running with $i
   newman run $collection --bail -e $i -n 2 --folder "${folder11}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing $folder11 FAILED" >> $file; fi
done

### Test for IOS XR
arr=("xrv_env.json" "asr_env.json")

for i in ${arr[@]}
do
   echo Collection running with $i
   newman run $collection --bail -e $i -n 1 --folder "XR ${folder1}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing XR $folder1 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "XR ${folder2}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing XR $folder2 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "XR ${folder3}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing XR $folder3 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "XR ${folder4}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing XR $folder4 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "XR ${folder5}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing XR $folder5 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "XR ${folder6}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing XR $folder6 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "XR ${folder7}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing XR $folder7 FAILED" >> $file; fi
     if [ "$i" == "asr_env.json" ]
     then
       newman run postman.json --bail -e $i -n 1 --folder "XR ${folder8}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing XR $folder8 FAILED" >> $file; fi
     fi
   newman run $collection --bail -e $i -n 1 --folder "XR ${folder9}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing XR $folder9 FAILED" >> $file; fi
done

### Test for IOS
arr=("classic_152_env.json" "classic_1553_env.json" "xe_env.json")

for i in "${arr[@]}"
do
   echo Collection running with $i
   newman run $collection --bail -e $i -n 1 --folder "Classic ${folder1}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing Classic $folder1 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "Classic ${folder2}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing Classic $folder2 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "Classic ${folder3}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing Classic $folder3 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "Classic ${folder4}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing Classic $folder4 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "Classic ${folder10}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing Classic $folder10 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "Classic ${folder6}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing Classic $folder6 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "Classic ${folder7}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing Classic $folder7 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "Classic ${folder12}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing Classic $folder7 FAILED" >> $file; fi
   newman run $collection --bail -e $i -n 1 --folder "Classic ${folder9}"; if [ "$?" != "0" ]; then echo "Collection $collection with environment $i testing Classic $folder9 FAILED" >> $file; fi
done



if [ -f $file ] ; then
    cat $file
    rm $file
fi
