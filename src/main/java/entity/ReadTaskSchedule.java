package entity;

import java.util.List;

/**
 * Created by 周思成 on  2020/5/8 17:26
 */

/**
 * A scheduled task for read
 */
public class ReadTaskSchedule {

   private List<ReadEvent> readEventList;
   private long index;
   private boolean finished =false;

   public List<ReadEvent> getReadEventList() {
      return readEventList;
   }

   public void setReadEventList(List<ReadEvent> readEventList) {
      this.readEventList = readEventList;
   }

   public long getIndex() {
      return index;
   }

   public void setIndex(long index) {
      this.index = index;
   }

   public boolean isFinished() {
      return finished;
   }

   public void setFinished(boolean finished) {
      this.finished = finished;
   }
}
