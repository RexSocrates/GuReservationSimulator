# GuReservationSimulator

This projects is a simulator that counts session success rate and the number of signals used to make network resource reservation. In this project, three reservation schemes are implemented, which are Fixed Scheme (FS), Multiplicative Scheme (MS) and Q model based reservation scheme (QRS).

## Introduction to Online Charging architecture

According to the specifications (32.240) of 3rd Generation Partnership Project (3GPP), charging mechanisms include Offline Charging and Online Charging. In online charging, authorization for network resource usage should be obtained by serving network prior to actual network resource usage to occur. 

The following figure shows the logical architecture of online charging.
![Imgur](https://i.imgur.com/JZ19ASO.png "Online Charging architecture")
CTF (Charging Trigger Function) detects the charging events, and sends requests to OCS (Online Charging System). OCS handles the authorization of network resource usage.

The following figure shows the process of online charging.
To know more details about each step, visit the specifications (32.296) of 3GPP.
![Imgur](https://i.imgur.com/ugapB16.png "Online Charging process")