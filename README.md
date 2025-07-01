# Distributed Inventory Management System

A Java-based client-server application for managing inventory across multiple branch locations with real-time stock requests and updates.

## Features

### Server (Central Warehouse)
- **GUI-based server application** with tabbed interface
- **Real-time inventory management** with add/remove stock functionality
- **Client connection monitoring** - view all connected branch clients
- **Comprehensive logging** - track all inventory operations and client requests
- **Multi-threaded** - handles multiple client connections simultaneously

### Client (Branch Locations)
- **Multi-tab interface** for different operations:
  - **Local Inventory** - view and manage branch stock
  - **Warehouse Stock** - view available products in central warehouse
  - **Request Stock** - request products from central warehouse
  - **Activity Logs** - view all transactions and communications
- **Real-time updates** when stock requests are approved/denied
- **Quick request buttons** for common products
- **Local sales tracking** - sell products from local inventory

### Communication Features
- **Socket-based communication** between client and server
- **Real-time inventory synchronization**
- **Request approval/denial system**
- **Automatic inventory updates** when changes occur

## System Architecture

```
┌─────────────────┐         ┌─────────────────┐
│  Central Server │◄────────┤  Branch Client  │
│   (Warehouse)   │         │   (Branch-1)    │
│                 │         │                 │
│  - Inventory    │         │ - Local Stock   │
│  - Client Mgmt  │         │ - Request Stock │
│  - Logging      │         │ - View Warehouse│
└─────────────────┘         └─────────────────┘
        ▲                           ▲
        │                           │
        └───────────────────────────┼─── TCP Socket (Port 5000)
                                    │
                            ┌─────────────────┐
                            │  Branch Client  │
                            │   (Branch-2)    │
                            │                 │
                            │ - Local Stock   │
                            │ - Request Stock │
                            │ - View Warehouse│
                            └─────────────────┘
```

## Getting Started

### Prerequisites
- Java 8 or higher
- Maven (for building, optional)

### Building the Project
```bash
# Easy way (Windows)
compile.bat

# Or manual compilation
javac -d target/classes -cp src/main/java src/main/java/com/project/*.java
```

### Running the Application

#### 1. Start the Central Server
```bash
# Easy way (Windows)
run-server.bat

# Or manual
java -cp target/classes com.project.CentralServer
```

#### 2. Start Branch Clients
```bash
# Easy way (Windows)
run-client.bat              # Creates Branch-1
run-client.bat "Branch-2"   # Creates Branch-2

# Or manual
java -cp target/classes com.project.BranchClientApp Branch-1
java -cp target/classes com.project.BranchClientApp Branch-2
```

#### 3. Run Client Simulation (NEW!)
```bash
# Easy way (Windows)
run-simulation.bat

# Or manual
java -cp target/classes com.project.SimulationLauncher
```

## How to Use

### Server Operations

1. **View Inventory**: The "Inventory" tab shows current warehouse stock
2. **Add Stock**: Click "Add Stock" to add new products or increase quantities
3. **Remove Stock**: Click "Remove Stock" to decrease product quantities
4. **Monitor Clients**: Use "Connected Clients" tab to see active branch connections
5. **Check Logs**: "Server Logs" tab shows all operations and client activities

### Client Operations

1. **Local Inventory Management**:
   - View current branch stock in "Local Inventory" tab
   - Use "Sell Product" to simulate sales and reduce local stock

2. **View Warehouse Stock**:
   - "Warehouse Stock" tab shows available products in central warehouse
   - Click "Refresh Warehouse" to get latest inventory

3. **Request Stock**:
   - Use "Request Stock" tab to request products from warehouse
   - Enter Product ID and quantity manually
   - Or use "Quick Request" buttons for common products

4. **Monitor Activity**:
   - "Activity Logs" tab shows all transactions and communications
   - Real-time updates when requests are approved/denied

### Simulation Operations (NEW!)

The simulation launcher allows you to test the server with multiple automated clients:

1. **Quick Simulation**: 5 clients with moderate activity
2. **Heavy Load**: 10 clients with high request frequency
3. **Light Load**: 3 clients with occasional requests
4. **Custom Setup**: Configure your own simulation parameters
5. **Monitor Status**: View all active simulated clients
6. **Stop All**: Terminate all simulated clients

Simulated clients automatically:
- Connect to the server without GUI
- Make random stock requests at configurable intervals
- Track their local inventory
- Respond to server updates
- Log all activities to console

## Product Catalog

The system comes pre-loaded with the following products:

| Product ID | Name     | Initial Warehouse Qty |
|------------|----------|----------------------|
| P001       | Pen      | 100                  |
| P002       | Notebook | 50                   |
| P003       | Pencil   | 75                   |
| P004       | Eraser   | 30                   |
| P005       | Ruler    | 25                   |

## Communication Protocol

The system uses a simple text-based protocol over TCP sockets:

### Client to Server Messages
- `REQUEST:ProductID:Quantity` - Request stock from warehouse
- `SHOW` - Request current warehouse inventory
- `PING` - Connection health check

### Server to Client Messages
- `APPROVED:ProductID:Quantity` - Stock request approved
- `DENIED:ProductID:Quantity` - Stock request denied (insufficient stock)
- `INVENTORY_UPDATE` - Start of inventory data
- `PRODUCT:ID:Name:Quantity` - Product information
- `INVENTORY_END` - End of inventory data

## Network Configuration

- **Default Port**: 5000
- **Server Address**: localhost (127.0.0.1)
- **Protocol**: TCP

To change the server port, modify the `PORT` constant in `CentralServer.java` and ensure clients connect to the same port.

## Troubleshooting

### Common Issues

1. **"Failed to connect to Central Server"**
   - Ensure the server is running before starting clients
   - Check that port 5000 is not blocked by firewall
   - Verify server is listening on correct port

2. **Client not receiving updates**
   - Check network connectivity
   - Restart the client application
   - Verify server logs for connection errors

3. **Stock requests not working**
   - Ensure valid Product ID is used
   - Check that sufficient stock exists in warehouse
   - Verify client is properly connected to server

### Logs Location
- Server logs are displayed in the "Server Logs" tab
- Client logs are shown in the "Activity Logs" tab
- Console output provides additional debugging information

## Future Enhancements

- Database persistence for inventory data
- User authentication and authorization
- Remote server deployment support
- Email notifications for stock alerts
- Reporting and analytics features
- Multi-warehouse support

## License

This project is created for educational purposes. 