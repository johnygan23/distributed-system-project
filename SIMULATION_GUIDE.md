# Client Simulation Guide

This guide explains how to use the **SimulationLauncher** to test your distributed inventory system with multiple automated clients.

## 🎯 Purpose

The simulation feature allows you to:
- **Test server performance** under different load conditions
- **Visualize real-time updates** across multiple clients simultaneously  
- **Stress test** the multi-threading and concurrency handling
- **Monitor inventory changes** as multiple clients make requests
- **Debug and optimize** the client-server communication

## 🚀 Quick Start

### Step 1: Start the Server
```bash
run-server.bat
```
Wait for the server GUI to appear and show "Central Server started on port 5000" in the logs.

### Step 2: Launch Simulation
```bash
run-simulation.bat
```

### Step 3: Choose Simulation Type
You'll see a menu with options:
```
--- Simulation Menu ---
1. Quick simulation (5 clients, moderate activity)
2. Heavy load simulation (10 clients, high activity)  
3. Light simulation (3 clients, low activity)
4. Custom simulation
5. Show client status
6. Stop all clients
7. Exit
```

### Step 4: Watch the Action!
- **Server GUI**: Watch the "Connected Clients" tab fill up with simulated clients
- **Server Logs**: See real-time request processing and inventory updates
- **Console Output**: View detailed client activities and requests

## 📊 Simulation Types

### 1. Quick Simulation (Recommended for first test)
- **Clients**: 5 simulated clients
- **Request Delay**: 3-7 seconds between requests
- **Request Size**: 1-5 items
- **Activity Level**: 60% chance to make request each cycle

**Best for**: Getting familiar with the simulation feature

### 2. Heavy Load Simulation  
- **Clients**: 10 simulated clients
- **Request Delay**: 1-4 seconds between requests
- **Request Size**: 1-8 items  
- **Activity Level**: 80% chance to make request each cycle

**Best for**: Stress testing server performance

### 3. Light Simulation
- **Clients**: 3 simulated clients
- **Request Delay**: 5-12 seconds between requests
- **Request Size**: 1-3 items
- **Activity Level**: 40% chance to make request each cycle

**Best for**: Long-running background testing

### 4. Custom Simulation
Configure your own parameters:
- Number of clients (1-20)
- Request delay range (milliseconds)
- Maximum request quantity  
- Request probability (0.0-1.0)

## 🔍 What to Observe

### In the Server GUI:

1. **Connected Clients Tab**:
   - Watch client count increase as simulated clients connect
   - Monitor connection status and timestamps

2. **Inventory Tab**:
   - See warehouse stock decrease as requests are approved
   - Watch real-time quantity updates

3. **Server Logs Tab**:
   - View detailed request processing
   - Monitor approval/denial decisions
   - Track inventory changes

### In the Console:

Each simulated client logs its activities:
```
[14:32:15] SimClient-1: Connected to server
[14:32:16] SimClient-1: Received inventory update - 5 products available  
[14:32:18] SimClient-1: Requesting 3 units of P001 (Warehouse has: 100)
[14:32:18] SimClient-1: ✓ APPROVED: P001 x3 (Local stock: 3)
[14:32:22] SimClient-1: Requesting 2 units of P003 (Warehouse has: 75)
[14:32:22] SimClient-1: ✓ APPROVED: P003 x2 (Local stock: 5)
```

## 🎛️ Simulation Control

### Monitor Client Status
Press `5` in the simulation menu to see:
- Which clients are running
- Local inventory summary for each client
- Total client count

### Stop All Clients
Press `6` to gracefully stop all simulated clients and clean up connections.

### Restart Simulation
You can stop one simulation and start another type without restarting the server.

## 📈 Advanced Testing Scenarios

### Scenario 1: Resource Depletion Test
1. Start Heavy Load simulation (10 clients)
2. Let it run for 2-3 minutes
3. Watch as warehouse stock depletes
4. Observe how clients handle DENIED responses

### Scenario 2: Mixed Client Testing
1. Start Light simulation (3 clients)
2. Open regular GUI clients (`run-client.bat`)
3. Make manual requests while simulation runs
4. Observe real-time updates across all clients

### Scenario 3: Server Stress Test
1. Use Custom simulation with:
   - 15-20 clients
   - 500-1000ms delay
   - High request probability (0.9)
2. Monitor server performance and responsiveness

## 🛠️ Troubleshooting

### Common Issues:

**"Failed to connect to server"**
- Ensure server is running first
- Check server logs for connection errors

**Simulation seems slow**
- Try reducing the delay range in custom simulation
- Use Heavy Load preset for faster activity

**Too many DENIED requests**
- Add more stock to warehouse using server GUI
- Reduce request quantities in custom simulation

**Clients not visible in server GUI**
- Refresh the Connected Clients tab
- Check console for connection errors

### Performance Tips:

- **For demonstrations**: Use Quick or Light simulation
- **For testing**: Use Heavy Load or Custom with many clients  
- **For long-running tests**: Use Light simulation overnight
- **For debugging**: Start with 1-2 clients in Custom mode

## 🎉 Demo Script

Perfect for showing off your distributed inventory system:

1. **Setup** (30 seconds):
   - Start server: `run-server.bat`
   - Show empty inventory and client tabs

2. **Add some warehouse stock** (30 seconds):
   - Use "Add Stock" button to add more products
   - Show updated inventory

3. **Launch simulation** (30 seconds):
   - Run `run-simulation.bat`
   - Choose option 1 (Quick simulation)

4. **Watch the magic** (2-3 minutes):
   - Point out connecting clients in "Connected Clients" tab
   - Show inventory decreasing in real-time
   - Highlight server logs showing request processing
   - Show console output with client activities

5. **Interactive demonstration** (2 minutes):
   - Open a GUI client (`run-client.bat "Demo-Branch"`)
   - Make manual requests while simulation runs
   - Show real-time updates across all clients

6. **Wrap up**:
   - Stop simulation (option 6)
   - Show final inventory state

Total demo time: ~6 minutes

This creates an impressive visualization of your distributed system handling multiple concurrent clients with real-time inventory synchronization! 