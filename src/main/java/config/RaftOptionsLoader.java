package config;

import core.NodeImpl;
import core.RaftGroupService;
import entity.Endpoint;
import entity.NodeId;
import entity.NodeOptions;
import entity.PeerId;
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

    Map map;
    public RaftOptionsLoader(String name) throws FileNotFoundException {
      this.map=  BootYaml.getYaml(name);
      LOG.info("load raftOptions from "+name+" options:"+map);
        Map nodeOptionsMap = (Map)map.get("NodeOptions");
        NodeOptions.getNodeOptions().setDaemon((boolean)nodeOptionsMap.get("daemon"));
        NodeOptions.getNodeOptions().setElectionTimeOut(((Integer)nodeOptionsMap.get("electionTimeOut")).longValue());
        NodeOptions.getNodeOptions().setMaxHeartBeatTime(((Integer)nodeOptionsMap.get("maxHeartBeatTime")).longValue());
        NodeOptions.getNodeOptions().setRpcProtocol((String)nodeOptionsMap.get("rpcProtocol"));
        NodeOptions.getNodeOptions().setSerialization((String)nodeOptionsMap.get("serialization"));
        NodeOptions.getNodeOptions().setPort((int)nodeOptionsMap.get("port"));
        Endpoint endpoint = new Endpoint((String)nodeOptionsMap.get("address"),(int)nodeOptionsMap.get("port"));
        PeerId peerId = new PeerId();
        peerId.setPeerName((String)nodeOptionsMap.get("name"));
        peerId.setId((String)nodeOptionsMap.get("peerId"));
        peerId.setEndpoint(endpoint);
        NodeId nodeId = new NodeId((String)nodeOptionsMap.get("groupId"),peerId);
        List<PeerId> listOtherNode = new CopyOnWriteArrayList<>();
           List list =   (List)map.get("OtherNodes");
        for (Object o:list
             ) {
            Map tempMap = (Map)o;
            PeerId peerId1 = new PeerId(tempMap.get("peerId").toString(),tempMap.get("name").toString()
                    ,tempMap.get("address").toString(),(Integer) tempMap.get("port"));
            listOtherNode.add(peerId1);
        }
        System.out.println(listOtherNode.size());
        NodeImpl.getNodeImple().setNodeId(nodeId);
    }


}
