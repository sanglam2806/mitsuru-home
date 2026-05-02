const sdt = @import("std");
const print = sdt.debug.print;
const c = @cImport({
    @cInclude("mongoc.h"); 
    @cInclude("bson.h"); 
});

pub fn main() void {
    print("Hello Moena Chan  {}", .{c});

    // _ = sdt.heap.page_allocator();

    c.mongoc_init();

    //init the mongo C driver
    // c.mongoc_init();

    // // clear after use
    // defer c.mongoc_cleanup();
    //
    // //Create new mongo Client
    // const uri = c.mongoc_uri_new("mongodb://localhost:27017");
    // if (uri == null) {
    //     print("Cannot connect to MongoDB", .{});
    //     return;
    // }

    print("Connectted", .{});

}
