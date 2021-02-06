#ECE 465 Cloud Computing

## Description 
This project is an implementation of the Two-phase commit protocol. There are three different test cases to test the performance of the algorithm. 

## Algorithm Description
The Two-Phase Commit (TPC/2PC) protocol is a type of atomic commitment protocol. It separates the database commit into two phases to ensure that each commit is correct and accounts for any problems that could occur. 

There is a transaction coordinator (TC), which drives the transactions and manages the commits to the database stores. The transaction coordinator makes sure either all systems do the action or do not and keeps track of which transaction is going on using the transaction ID (TID). The two phases are the Prepare phase and the Commit phase. 

In the Prepare phase, each database store sends a "DONE" message once it locally completed its transaction. Once the TC receives the message from all participants, it sends them a "PREPARE" message. Once each participant receives the "PREPARE" message, they send a "READY", "NOT READY", or no response at all. IF the TC doesn't receive a "READY" message from all participants then it sends a global "ABORT" message. The TC needs to receive an acknowledgment from all participants that the transaction has been aborted to consider the transaction aborted.  

The Commit phase occurs once the TC receives the "READY" message from all participants. The TC sends a "COMMIT" message, which contains the information of the transaction that needs to be stored in the databases. Once the participants have gone through the transaction, then it sends a "DONE" acknowledgment message back to the TC. After the TC receives the "DONE" message from all participants, then will the TC consider the entire transaction completed.

## To Run it
```$xslt
git clone https://github.com/Minhtyyufa/CloudComputing.git
cd CloudComputing
./build.sh
cd two_phase_commit
mvn exec:java -Dexec.mainClass="main.java.com.company.Main"
```

## Resources
- This helped a lot with logback and threads https://mkyong.com/logging/logback-different-log-file-for-each-thread/
- Most of the tools have come from https://github.com/robmarano. Thanks Professor!