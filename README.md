# Two-Phase Commit 
## Description
The Two-Phase Commit (TPC/2PC) protocol is a type of atomic commitment protocol. It separates the database commit into two phases to ensure that each commit is correct and accounts for any problems that could occur. 

There is a transaction coordinator (TC), which drives the transactions and manages the commits to the database stores. The transaction coordinator makes sure either all systems do the action or they all do not and keeps track of which transaction is going on using the transaction ID (TID). The two phases are the Prepare phase and the Commit phase. 

In the Prepare phase, each database store sends a "DONE" message once it locally completed its transaction. Once the TC receives the message from all participants, it sends them a "PREPARE" message. Once each participant receives the "PREPARE" message, they send a "READY", "NOT READY", or no response at all. IF the TC doesn't receive a "READY" message from all participants then it sends a global "ABORT" message. The TC needs to receive an acknowledgment from all participants that the transaction has been aborted to consider the transaction aborted.  

The Commit phase occurs once the TC receives the "READY" message from all participants. The TC sends a "COMMIT" message, which containes the information of the transaction that needs to be stored in the databases. Once the participants have gone through the transaction, then it sends a "DONE" acknowledgment messabe back to the TC. After the TC receives the "DONE" message from all participants, then will the TC consider the entire transaction completed
