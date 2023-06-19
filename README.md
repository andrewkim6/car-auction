
# Car Auction

This project was created to simultate a car auction where multiple clients can connect to one server and make requests to set bids on a car. The clients are initially met with a login screen where they can login with an existing account, a guest account, or create a new account with a username and password. If the user inputs the correct username and password they will be taken to the auction page. Data for account information and car information are held in separate collections within MongoDB that the server accesses. Multiple clients can send requests to the server, which is hosted through an AWS EC2 instance. Through these requests, the server will update the database hosted on MongoDB using server-side serialization to determine what object is sent, and send back information to the clients so that their GUI's will be updated to show accurate information.


## Tech Stack

**Languages:** Java


**Client:** Java Client


**Server:** AWS


**Database:** MongoDB


**Libraries:** JavaFX, Jasypt, BCrypt, BSON, GSON 






## Features

- List view of available cars with images
- Live expiration timers for each car
- Live history for bids placed and cars sold
- User and password creation
- Password encryption and hashing
- Unique buy price for each vehicle (Vehicle sold when bid reaches a set price)
- Server hosted on AWS EC-2 instance for easy access
- Minimum starting price on each item
- Custom item descriptions


## Keys Required

To run this project, you will need to have the following AMAZON_SSH_KEY, however due to privacy this won't be shared

`AMAZON_SSH_KEY`


## FAQ

#### How can you add new items to the auction or change item values?

The server initially parses through a text file and inputs the information on MongoDB automatically. Therefore all you have to do is edit the text file.

#### What is the user.java and bid.java class?

Based on what action the client goes through, it will sent either an object of type user or bid. When the server receives this object, it will determine what action is necessary to process the request.

#### How does the program query the MongoDB database?

Based on the user inputs, the program will determine what item is selected and query for that specific item in the collection of vehicles using java with MongoDB libraries.

#### Can this program be run locally?

Yes! In order to run the code locally, the server and client should be run on separate IDE's. Some modification must also be done in order for the server to connect to a local port rather than AWS.

#### What are the major concepts used in this project?

The main concepts used in this project are multithreading, serialization, and GUI development.


![Logo](https://icon-library.com/images/java-icon-png/java-icon-png-16.jpg)
![Logo](https://cdn.iconscout.com/icon/free/png-256/free-aws-1869025-1583149.png)
![Logo](https://cdn.iconscout.com/icon/free/png-256/free-mongodb-2-1175137.png)
