# Welcome to [Geoweaver](https://esipfed.github.io/Geoweaver/)

2018 ESIP Lab Incubator Project

Geoweaver is a web system to allow users to easily compose and execute full-stack Long Short Term Memory (LSTM) Recurrent Neural Network (RNN) workflows in web browsers by taking advantage of the online spatial data facilities, high-performance computation platforms, and open-source deep learning libraries.

# Project Goals

1) turning large-scale distributed deep network into manageable modernized workflows;

2) boosting higher utilization ratio of the existing cyberinfrastructures by separating scientists from
tedious technical details;

3) enhancing the frequency and accuracy of classified land cover land use maps for agricultural purposes;

4) enabling the tracking of provenance by recording the execution logs in structured tables to evaluate the
quality of the result maps;

5) proof the effectiveness of operationally using large-scale distributed LSTM network in classifying
Landsat image time series.

# Installation

## Prerequisite

Ubuntu 16.04+

JDK 1.8+

Tomcat 8.0+

Maven 3.5+ (for building from source)

## Quick Install

### Tomcat War

To use Geoweaver, download and copy the war to the webapps directory of Tomcat. Start Tomcat. Enter the following URL into browser address bar to open Geoweaver:

`http://your-ip:your-port/Geoweaver-<version>/web/geoweaver`

Replace the `Geoweaver-<version>` with the real name of your download Geoweaver package. For example, `Geoweaver-0.6.6`.

### Cloud VM Template

We provide a ready-to-use cloud template for you to install on mainstream cloud platforms like AWS, Google Cloud, Azure, OpenStack and CloudStack. Please go here to download the template.

### Docker

We published a Docker image in DockerHub for docker users. 

## Build from source

Use maven to build. In the command line go to the root folder and execute `mvn install`. After a success build, the Geoweaver war package will be under the directory: `Geoweaver/target/Geoweaver-<version>.war`.

## Usage

### Add A Server

Enroll a server to Geoweaver is simple. The server must have SSH server installed and enabled.

![Add a host](docs/addhost.gif)

### Create A Process

Geoweaver supports Bash Shell scripts as processes. You can write bash command lines in the code area. Note: the commands should exist on the target hosts.

![Add a process](docs/addprocess.gif)

### Create A Workflow

Geoweaver can link the processes together to form a workflow.

![Create a workflow](docs/createworkflow.gif)

### Run Workflow

Geoweaver can run the created workflows on the enlisted servers. During the running, Geoweaver is monitoring the status of each process. The color of process text in their circles indicate the process status. Yellow means running, green means completed, and red means failure.

![Run a workflow](docs/runworkflow.gif)

### Browse Provenance

Geoweaver stores all the inputs and outputs of each process run. Users can check the workflow provenance by simply clicking.

![Check provenance](docs/checkprovenance.gif)

### Retrieve and Display Results

Geoweaver can retrieve the result files of the executed workflows and visualize them if the format is supported (png, jpg, bmp, etc. The list is expanding. I am on it.).

![Get result](docs/getresult.gif)

### I/O workflows

The workflows can be exported and move around and imported back.

![Export workflow](docs/exportworkflow.gif)

# Demonstration

A live demo site is available in George Mason University: [I am a link, hit me](http://cube.csiss.gmu.edu/CyberConnector/web/geoweaver).

Here is a use case of Geoweaver, using LSTM RNN to classify landsat images into agricultural land use maps.  

![Geoweaver user interface](/geoweaver-ui.png)

# Documentation

[Project Proposal](docs/geoweaver-proposal-revised-v4.pdf)

[August Report](docs/ESIP-Geoweaver-Report-1.docx)

[September Report](docs/ESIP-Geoweaver-Report-2.docx)

[October Report](docs/ESIP-Geoweaver-Report-3.docx)

## Open Source Libraries

This project is impossible without the support of several fantastic open source libraries.

[d3.js](https://github.com/d3/d3) - BSD 3-Clause

[graph-creator](https://github.com/cjrd/directed-graph-creator) - MIT License

[bootstrap](https://github.com/twbs/bootstrap) - MIT License

### Personnel

Ziheng Sun, zsun@gmu.edu

Liping Di, ldi@gmu.edu

Center for Spatial Information Science and Systems

George Mason University

