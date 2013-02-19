#!/bin/bash
keytool -genkey -alias $1 -keyalg RSA -keysize 512 -keystore blogracy.jks -storepass blogracy -keypass blogracy
