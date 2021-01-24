class Vec3 { 
    float x, y, z; 
    Vec3(float xx, float yy, float zz) {
        x = xx; y = yy; z = zz;
    }
    float length() {
        return (float)CaveGen.sqrt(x*x+y*y+z*z);
    }
    Vec3 add(Vec3 o) {
        return new Vec3(x+o.x,y+o.y,z+o.z);
    }
    Vec3 subtract(Vec3 o) {
        return new Vec3(x-o.x,y-o.y,z-o.z);
    }
    Vec3 scale(float d) {
        return new Vec3(x*d,y*d,z*d);
    }
    float dot(Vec3 o) {
        return x*o.x + y*o.y + z*o.z;
    }
    Vec3 copy() {
        return new Vec3(x,y,z);
    }
    float dist(Vec3 o) {
        return (float)CaveGen.sqrt((o.x-x)*(o.x-x) + (o.y-y)*(o.y-y) + (o.z-z)*(o.z-z));
    }
    float dist2(Vec3 o) {
        return (float)CaveGen.sqrt((o.x-x)*(o.x-x) + (o.z-z)*(o.z-z));
    }
    Vec3 normalize() {
        float length = length();
        if (length > 0)
            return scale(1 / length);
        return copy();
    }
    String string() {
        return "(" + x + "," + y + "," + z + ")";
    }
}