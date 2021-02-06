# Two-Phase Commit 
## Algorithm Description
The Two-Phase Commit (TPC/2PC) protocol is a type of atomic commitment protocol. It separates the database commit into two phases to ensure that each commit is correct and accounts for any problems that could occur. 

There is a transaction coordinator (TC), which drives the transactions and manages the commits to the database stores. The transaction coordinator makes sure either all systems do the action or do not and keeps track of which transaction is going on using the transaction ID (TID). The two phases are the Prepare phase and the Commit phase. 

In the Prepare phase, each database store sends a "DONE" message once it locally completed its transaction. Once the TC receives the message from all participants, it sends them a "PREPARE" message. Once each participant receives the "PREPARE" message, they send a "READY", "NOT READY", or no response at all. IF the TC doesn't receive a "READY" message from all participants then it sends a global "ABORT" message. The TC needs to receive an acknowledgment from all participants that the transaction has been aborted to consider the transaction aborted.  

The Commit phase occurs once the TC receives the "READY" message from all participants. The TC sends a "COMMIT" message, which contains the information of the transaction that needs to be stored in the databases. Once the participants have gone through the transaction, then it sends a "DONE" acknowledgment message back to the TC. After the TC receives the "DONE" message from all participants, then will the TC consider the entire transaction completed.


## Implementation
There are four classes to implement TPC: main, controller, participant, and message. The Main class creates and starts a new thread for the transaction controller and each participant. The Controller and Participant class implements the Runnable interface, which is executed when the thread is created. In the Controller class, the IDs of the participants and the different commands are listed. The controller simultaneously sends each of the commands to all the participants. The participants will parse through the commands; if the command is "ABORT", each participant will revert any changes done by unlocking the write lock so others can look at the thread. Then each participant will send an "ACKABORT" message to acknowledge that they have aborted the transaction. However, if the message is not "ABORT", the participant will handle the command: add or subtract. After the action has been executed, the new balance is checked to make sure it is not negative. If the new balance is negative, the participant will send a "NO" message to the controller, otherwise it will send a "YES" message. If the controller does not receive a "YES" message from all the participants, it will abort all transactions. If all the participants say yes, then the controller will send out a "COMMIT" message to all the participants. Once each participant receives the "COMMIT" message, the new balance because the actual balance. Then each participant sends their ID as well as an "ACK" message to acknowledge that they have received the commit message. 

## Algorithm
Our Algorithm is Worst-Case O(N) where N is the number of commands. In the worst-case, all of the commands would be sent to the same participant

## Test Cases
Provided are three different test cases (found in the [test](./test) directory): Easy Abort, Easy Success, and Hard.
Easy Abort contains 10 commands that will result in an illegal state triggering an abort. Easy Success contains 10 commands that
will result in a legal state allowing for a commit. Both of the Easy test cases have 3 participants, but only use 2 in the commands.
The Hard Test case contains 10,000 commands and 26 participants and will result in an abort operation. Their respective times
are shown below:

1st Revision Times

|   Test Case  | Time (milliseconds) |
|:------------:|:-------------------:|
|  Easy Abort  |         203         |
| Easy Success |         184         |
|     Hard     |         5370        |

2nd Revision Times

|   Test Case  | Time (milliseconds) |
|:------------:|:-------------------:|
|  Easy Abort  |         156         |
| Easy Success |           88       |
|     Hard     |         1260        |

From these times, we can see that the algorithm our algorithm is O(N). The Hard test case is performing significantly better than 1000x worse than the Easy cases
because of the parallelization. 

## Dependencies
- [Logback](http://logback.qos.ch/)

## To Run it
```$xslt
git clone https://github.com/Minhtyyufa/CloudComputing.git
cd CloudComputing
./build.sh
cd two_phase_commit
mvn exec:java -Dexec.mainClass="main.java.com.company.Main"
```

## What's new in Revision 2?

- Added toolings and made it easier to copy
- Made improvements to the algorithm as seen in the [Test Cases](#test-cases) section
    - Added a separate message queue for each participant so that they don't block each other and can perform their actions faster.
    - This signifantly reduces the amount of dead time that each participant goes through per transaction.


## Resources
- This helped a lot with logback and threads https://mkyong.com/logging/logback-different-log-file-for-each-thread/
- Most of the tools have come from https://github.com/robmarano. Thanks Professor!