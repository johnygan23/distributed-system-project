Project: Distributed Inventory System

This README provides simple instructions to compile and run the Distributed Inventory System project.

1.) Run CentralServer.java: The CentralServer application must be running before any clients connect.
    i) A GUI window titled "Central Warehouse Server" will appear.
    ii) Observe the "Inventory", "Connected Clients", and "Server Logs" tabs.

2.) Run Clients (BranchClientApp.java or SimulationLauncher.java)

    i) Run BranchClientApp.java: You can open multiple instances of the BranchClientApp.
       Optionally, you can provide a branch name as a command-line argument.
       Output:
            A GUI window titled "Branch Client - [Branch Name]" will appear.
            Observe the "Local Inventory", "Warehouse Stock", and "Client Logs" tabs.
            Use the "Request Stock" button to interact with the server.

    ii) Run SimulationLauncher.java: You can simulate multiple SimulatedClients interacting with the server.
        Output:
            A console menu will appear.
            Choose options like "High Contention" or "High Concurrency" to observe different lock behaviors on the server's log.
            The output of SimulatedClients will appear in the same terminal window as the SimulationLauncher.

3.) Observe the project
    i) Central Server GUI (Server Logs tab): This is crucial for observing the lock status (acquisition and release), request processing, and general server activity. Pay attention to timestamps.

    ii) Central Server GUI (Inventory tab): Watch product quantities change in real-time as requests are processed by clients.

    iii) Branch Client GUI: See your local inventory and the updated warehouse stock.

    iv) Simulation Launcher Console: Observe the stream of requests sent by simulated clients and their responses.