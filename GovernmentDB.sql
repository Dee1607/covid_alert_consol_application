/*
*********************************************************************
Name: GovernmentDatabase
*********************************************************************
*/

/*
Creating database with name: GovernmentDatabase
*/
CREATE DATABASE `GovernmentDatabase`;

USE `GovernmentDatabase`;

/*Table structure
Table Name: mobiledevices
Filds: 
	id:- auto incremented primary key element
    deviceHash:- Unique identity of each device
    testHash:- unique test code for each device user
*/
CREATE TABLE `mobiledevices`(
	id INT AUTO_INCREMENT PRIMARY KEY,
	deviceHash VARCHAR(255) NOT NULL,
	testHash VARCHAR(255)
);


/*Table structure
Table Name: contacts
Filds: 
	id:- auto incremented primary key element
    deviceHash:- Unique identity of each device
    contactHash:- Unique identity of each contacted device
    contactDate:- date of contact
    contactDuration:- duration od contact
*/
CREATE TABLE `contacts`(
	id INT AUTO_INCREMENT PRIMARY KEY,
	deviceHash VARCHAR(255),
	contactHash VARCHAR(255) ,
    contactDate INT ,
    contactDuration INT
);


/*Table structure
Table Name: `testinfo`
Filds: 
	id:- auto incremented primary key element
    testHash:- unique test code for each device user
	testDate:- date of test
    testResult:- result of the covid-19 test
*/
CREATE TABLE `testinfo`(
	id int AUTO_INCREMENT PRIMARY KEY,
    testHash VARCHAR(255) REFERENCES mobiledevices.testHash,
	testDate INT,
    testResult VARCHAR(50)
);


/*Table structure
Table Name: `alertinfo`
Filds: 
	id:- auto incremented primary key element
    deviceHash:- Unique identity of each device
    contactHash:- Unique identity of each contacted device
    contactDate:- date of contact
*/
CREATE TABLE `alertinfo`(
	id INT AUTO_INCREMENT PRIMARY KEY,
    deviceHash VARCHAR(255),
    contactHash VARCHAR(255),
    contactDate INT
);