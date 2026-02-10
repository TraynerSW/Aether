#include "server.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char* argv[]) {
    (void)argc;
    (void)argv;
    
    server_config_t config = {
        .bind_address = "0.0.0.0",
        .port = SERVER_DEFAULT_PORT,
        .backlog = SERVER_DEFAULT_BACKLOG,
        .database_path = "data/securechat.db",
        .cert_path = NULL,
        .key_path = NULL,
        .use_tls = false,
        .log_level = 1
    };
    
    server_context_t* server = server_create(&config);
    if (!server) {
        fprintf(stderr, "Failed to create server\n");
        return 1;
    }
    
    int result = server_run(server);
    
    server_destroy(server);
    
    return result;
}
