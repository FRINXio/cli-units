# CLI-UNITS:POSTMAN
Generic scenario for tests:
- Get data root before setup and check that in the response there is no value 
that will be created in SETUP (e.g. get all interfaces and assert Loopback1 is not present)
- Get particular data before setup and check that in response there is no value 
from SETUP (e.g. get Loopback1 interface and assert 404)
- SETUP (e.g. create Loopback interface)
- Get data root after setup and verify that data from SETUP is present 
(e.g. get all interfaces and check that Loopback1 is there)
- Get particular data and check expected output (e.g. get the single Loopback1 interface)
- TEAR DOWN


## Requirements
postman 5.3.1
newman 3.8.2


## Installation
1. Download and unzip distribution
2. Start distro with token
3. Install features: odl-restconf cli-southbound-plugin 
unified-topology-all-units cli-southbound-all-units odl-netconf-connector-all
4. Run /.test.sh


## Usage
Test for cli is divided into IOS XR, IOS Classic
and Mount/Unmount IOS folders. 
IOS XR folder is executable with xrv_env.json (variables for
virtual IOS XR device) and asr_env.json (variables for
physical IOS XR device)
IOS Classic folder is executable with classic_env.json (IOS Classic device)
and xe_env.json (IOS XE device). 
Mount/Unmount folder is executable with mount_unmount_env.json (for
ssh protocol) and mount_unmount_telnet_env.json (telnet protocol)
In case you need use different variables, you can change it in 
*_env.json
