class DoorLink {
    float dist;
    int otherIdx;    
    int tekiFlag;

    DoorLink copy() {
        DoorLink l = new DoorLink();
        l.dist = dist;
        l.otherIdx = otherIdx;
        l.tekiFlag = tekiFlag;
        return l;
    }
}
