# RocksRaft

[![LICENSE](https://img.shields.io/hexpm/l/plug)](https://github.com/CoderiGenius/RocksRaft/blob/master/LICENSE)

- 基于Raft一致性协议的分布式存储系统，参考阿里巴巴SOFAJRaft并使用Java从零实现。
- Distributed storage system based on Raft consistency protocol, referencing Alibaba SOFAJRaft and implemented from scratch using Java
## Reference
- Alibaba [SOFAJRaft](https://github.com/sofastack/sofa-jraft)
## Related papers
- [Graduation thesis of QUST in 2020 for bachelor's degree ](https://github.com/CoderiGenius/RocksRaft/blob/master/%E8%AE%BA%E6%96%87github%E5%BC%80%E6%BA%90%E7%89%88.pdf)
## Quick start
- Basic requirement
  - JDK 1.8+
  - IntelliJ idea
  - maven
- Clone project
````
git clone https://github.com/CoderiGenius/RocksRaft.git
````
- Import in to idea as maven project
- Config and start the example of 3 nodes scenario. 
    - Config the three yml files in **src/main/resources**
    - Start the main function with files listed below
        - src/main/java/Start.java
        - src/main/java/Start2.java
        - src/main/java/Start3.java
- Wait until the nodes come up.
- Run the src/main/java/ClientExample.java
- You should be able to see the log of appendEnties and the procedures of applying to statemachine.
- Log example:
```
WARN:appendEntriesRequest CHECK:index 0 dataIsEmpty:false 
WARN:checkBallotBoxToApply applied:Voting currentIndex:0 length:1 stableLogIndex:0 
INFO:Ballot box invokes apply at index 0 length 1 with grant list [PeerId{endpoint=localhost:12220, checksum=0}, PeerId{endpoint=localhost:12230, checksum=0}]
INFO:doCommitted at 1 iterImpl:IteratorImpl [fsm=core.CustomStateMachine@22d116d, logManager=core.LogManagerImplNew@61295f0, currentIndex=1, committedIndex=1, currEntry=entity.LogEntry@ceafe80, applyingIndex=1, error=null,isGood=true] isGood:true
INFO:Apply to fsm at 1
INFO:Log 1 has been applied to stateMachine
INFO:Receive follower raft-3 applied  firstIndex :0 lastIndex:0
INFO:Receive follower raft-1 applied  firstIndex :0 lastIndex:0
```
