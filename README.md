# Blogracy #

## Introduction ##

Blogracy is a simple peer-to-peer social networking system, built on top of Bittorrent.

## Install ##

The installation process is not yet streamlined. Essentially Blogracy is composed
by two main modules:

1. a Vuze plugin
2. a web application

The build system uses Maven. If you have a working  maven
installation things should be rather smooth.

### Getting Blogracy ###

The preferred way to obtain blogracy sources is with git. Blogracy main
repository is hosted at git://github.com/rik0/blogracy.git. However,
if you intend to contribute to blogracy, we strongly encourage you to make a
fork and then pull request the modifications.

#### Fork ####

1. Create a fork through GitHub repository
2. The fork repository will be at git@github.com:\<USERNAME\>/blogracy.git
   where \<USERNAME\> is your actual username
3. Clone the new git fork:

```
git clone git@github.com:<USERNAME>/blogracy.git
```

If you are using Eclipse, issue the command directly in the workspace or
move the sources afterwards.

#### Read-Only ####

To get the sources "read only" simply:

```
git://github.com/rik0/blogracy.git
```

### Compile  and Run ###

Issue the following command:

```
./build.sh
```

For more detailed information, read the instructions in the folder of each module:

* [blogracy-vuze](blogracy-vuze)
* [blogracy-web](blogracy-web)

## Authors ##

The code of the present versions is mostly written by Enrico Franchi and Michele Tomaiuolo.

The project is currently maintained by Enrico Franchi and Michele Tomaiuolo.

## Contributors ##

* Enrico Franchi (efranchi@ce.unipr.it) [ core ]
* Michele Tomaiuolo (tomamic@ce.unipr.it) [ core ]
* Alan Nonnato () [ old core ]
