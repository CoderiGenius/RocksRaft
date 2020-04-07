package config;

import core.NodeImpl;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.BootYaml;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by 周思成 on  2020/3/13 12:03
 */

public class RaftOptionsLoader {
    private static final Logger LOG     = LoggerFactory.getLogger(RaftOptionsLoader.class);

    public  RaftOptionsLoader(String name) throws FileNotFoundException {
        Options options = BootYaml.getYaml(name);
        NodeImpl.getNodeImple().setOptions(options);
        LOG.info("load raftOptions from "+name+" options:"+options.toString());
        CurrentNodeOptions currentNodeOptions = options.getCurrentNodeOptions();
        OtherNodes[] otherNodes = options.getOtherNodes();

        NodeOptions.getNodeOptions().setDaemon(currentNodeOptions.isDaemon());
        NodeOptions.getNodeOptions().setElectionTimeOut(currentNodeOptions.getElectionTimeOut());
        NodeOptions.getNodeOptions().setMaxHeartBeatTime(currentNodeOptions.getMaxHeartBeatTime());
        NodeOptions.getNodeOptions().setRpcProtocol(currentNodeOptions.getRpcProtocol());
        NodeOptions.getNodeOptions().setSerialization(currentNodeOptions.getSerialization());
        NodeOptions.getNodeOptions().setPort(currentNodeOptions.getPort());
        Endpoint endpoint = new Endpoint(currentNodeOptions.getAddress(),currentNodeOptions.getPort());
        PeerId peerId = new PeerId();
        peerId.setPeerName(currentNodeOptions.getName());
        peerId.setId(currentNodeOptions.getPeerId());
        peerId.setEndpoint(endpoint);
        NodeId nodeId = new NodeId(currentNodeOptions.getGroupId(),peerId);
        List<PeerId> listOtherNode = new CopyOnWriteArrayList<>();

        for (OtherNodes o:otherNodes
        ) {

            PeerId peerId1 = new PeerId(o.getPeerId(),o.getName()
                    ,o.getAddress(),o.getPort());
            listOtherNode.add(peerId1);
        }

        NodeImpl.getNodeImple().setNodeId(nodeId);
    }


    Map map;
//    public void RaftOptionsLoader0(String name) throws FileNotFoundException {
//      //this.map=  BootYaml.getYaml(name);
//      LOG.info("load raftOptions from "+name+" options:"+map);
//        Map nodeOptionsMap = (Map)map.get("NodeOptions");
//        NodeOptions.getNodeOptions().setDaemon((boolean)nodeOptionsMap.get("daemon"));
//        NodeOptions.getNodeOptions().setElectionTimeOut(((Integer)nodeOptionsMap.get("electionTimeOut")).longValue());
//        NodeOptions.getNodeOptions().setMaxHeartBeatTime(((Integer)nodeOptionsMap.get("maxHeartBeatTime")).longValue());
//        NodeOptions.getNodeOptions().setRpcProtocol((String)nodeOptionsMap.get("rpcProtocol"));
//        NodeOptions.getNodeOptions().setSerialization((String)nodeOptionsMap.get("serialization"));
//        NodeOptions.getNodeOptions().setPort((int)nodeOptionsMap.get("port"));
//        Endpoint endpoint = new Endpoint((String)nodeOptionsMap.get("address"),(int)nodeOptionsMap.get("port"));
//        PeerId peerId = new PeerId();
//        peerId.setPeerName((String)nodeOptionsMap.get("name"));
//        peerId.setId((String)nodeOptionsMap.get("peerId"));
//        peerId.setEndpoint(endpoint);
//        NodeId nodeId = new NodeId((String)nodeOptionsMap.get("groupId"),peerId);
//        List<PeerId> listOtherNode = new CopyOnWriteArrayList<>();
//           List list =   (List)map.get("OtherNodes");
//        for (Object o:list
//             ) {
//            Map tempMap = (Map)o;
//            PeerId peerId1 = new PeerId(tempMap.get("peerId").toString(),tempMap.get("name").toString()
//                    ,tempMap.get("address").toString(),(Integer) tempMap.get("port"));
//            listOtherNode.add(peerId1);
//        }
//        System.out.println(listOtherNode.size());
//        NodeImpl.getNodeImple().setNodeId(nodeId);
//    }
}
