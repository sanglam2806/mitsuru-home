const std = @import("std");
const httpz = @import("httpz");

pub fn main() !void {
    var gpa = std.heap.GeneralPurposeAllocator(.{}){};
    const allocator = gpa.allocator();

    var server = try httpz.Server(void).init(allocator, .{.port = 5882}, {});
    defer {
        server.stop();
        server.deinit();
    }

    var router = server.router(.{});
    router.get("/api/user/:id", getUser, .{});

    try server.listen(); 
}

fn getUser(req: *httpz.Request, res: *httpz.Response) !void {
    res.status = 200;
    try res.json(.{.id = req.param("id").?, .name = "Teg"}, .{});
}
