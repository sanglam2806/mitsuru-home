const sdt = @import("std");
const print = sdt.debug.print;
const c = @cImport({
    @cInclude("mongoc.h"); 
    @cInclude("bson.h"); 
});

pub fn main() void {
    print("Hello Moena Chan  {}", .{c});
}
