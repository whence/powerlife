using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Reactive.Subjects;
using System.Text;
using System.Windows.Threading;
using Bonjour;
using IdeaFactory.Util;

namespace Powercards.WinPresent
{
    internal class Communicator : IDisposable
    {
        #region constants
        private const string bonjourServiceType = "_powcarg._udp";
        #endregion

        #region fields
        private readonly OperationMode operationMode;
        private readonly DNSSDService localService;
        private DNSSDService localBrowser;
        private DNSSDService localRegistrar;
        private readonly List<ServiceInfo> remoteServiceInfos;
        private ServiceInfo localServiceInfo;
        private ServiceInfo resolvingInfo;
        private DNSSDService resolver;
        private readonly DNSSDEventManager eventManager;
        private readonly Socket socket;
        private readonly Subject<IPEndPoint> newRemoteEndPointUpdates;
        #endregion

        #region properties
        public IObservable<IPEndPoint> NewRemoteEndPointUpdates
        {
            get { return newRemoteEndPointUpdates; }
        }
        #endregion

        #region constructors
        public Communicator(int readBufferSize, Dispatcher dispatcher, Action<string> updater, OperationMode operationMode)
            : this(new SocketController(
                new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp), false,
                new byte[readBufferSize], dispatcher, updater), operationMode)
        {
        }

        public Communicator(int readBufferSize, Dispatcher dispatcher, Action<string, IPEndPoint> updater, OperationMode operationMode)
            : this(new SocketController(
                new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp), true,
                new byte[readBufferSize], dispatcher, updater), operationMode)
        {
        }

        private Communicator(SocketController controller, OperationMode operationMode)
        {
            this.operationMode = operationMode;
            
            this.socket = controller.Socket;
            this.socket.Bind(new IPEndPoint(IPAddress.Any, 0));

            if (controller.EnablePacketInformation)
                this.socket.SetSocketOption(SocketOptionLevel.IP, SocketOptionName.PacketInformation, true);

            EndPoint remoteEndPoint = new IPEndPoint(IPAddress.Any, 0);
            this.socket.BeginReceiveFrom(controller.Buffer, 0, controller.Buffer.Length, 
                SocketFlags.None, ref remoteEndPoint, OnReceivedFromSocket, controller);

            this.localService = new DNSSDService();
            this.remoteServiceInfos = new List<ServiceInfo>();

            this.eventManager = new DNSSDEventManager();
            this.eventManager.ServiceRegistered += eventManager_ServiceRegistered;
            this.eventManager.ServiceFound += eventManager_ServiceFound;
            this.eventManager.ServiceLost += eventManager_ServiceLost;
            this.eventManager.ServiceResolved += eventManager_ServiceResolved;
            this.eventManager.QueryRecordAnswered += eventManager_QueryRecordAnswered;
            this.eventManager.OperationFailed += eventManager_OperationFailed;

            this.newRemoteEndPointUpdates = new Subject<IPEndPoint>();
        }
        #endregion

        #region methods
        public void Start()
        {
            switch (this.operationMode)
            {
                case OperationMode.BrowseOnly:
                    Browse();
                    break;

                case OperationMode.PublishOnly:
                case OperationMode.PublishThenBrowse:
                    Publish();
                    break;
            }
        }

        private void Publish()
        {
            if (this.localRegistrar != null)
                throw new CustomException<Communicator>("The communicator is already publishing");

            if (this.localBrowser != null)
                throw new CustomException<Communicator>("The communicator cannot publish while browsing");

            var localEndPoint = (IPEndPoint)this.socket.LocalEndPoint;
            this.localRegistrar = this.localService.Register(0, 0, null, bonjourServiceType, null, null, (ushort)localEndPoint.Port, null, this.eventManager);
        }

        private void Browse()
        {
            if (this.localBrowser != null)
                throw new CustomException<Communicator>("The communicator is already browsing");

            if (this.localRegistrar != null && this.localServiceInfo == null)
                throw new CustomException<Communicator>("Please wait for registering finished before browsing");

            this.localBrowser = this.localService.Browse(0, 0, bonjourServiceType, null, this.eventManager);
        }

        private void eventManager_ServiceRegistered(DNSSDService service, DNSSDFlags flags, string name, string regtype, string domain)
        {
            this.localServiceInfo = new ServiceInfo(null, name, regtype, domain);

            switch (this.operationMode)
            {
                case OperationMode.PublishThenBrowse:
                    Browse();
                    break;
            }
        }

        private void eventManager_ServiceFound(DNSSDService browser, DNSSDFlags flags, uint ifIndex, string serviceName, string regtype, string domain)
        {
            if (this.localServiceInfo == null || !ServiceInfo.Match(this.localServiceInfo, serviceName, regtype, domain))
            {
                var info = new ServiceInfo(ifIndex, serviceName, regtype, domain);
                remoteServiceInfos.Add(info);
            }

            // ReSharper disable BitwiseOperatorOnEnumWihtoutFlags
            if ((flags & DNSSDFlags.kDNSSDFlagsMoreComing) != DNSSDFlags.kDNSSDFlagsMoreComing)
            // ReSharper restore BitwiseOperatorOnEnumWihtoutFlags
            {
                if (this.resolvingInfo == null)
                {
                    StartResolving();
                }
            }
        }

        private void StartResolving()
        {
            this.resolvingInfo = this.remoteServiceInfos.FirstOrDefault(x => !x.Resolved);
            if (this.resolvingInfo != null)
            {
                this.resolver = this.localService.Resolve(0,
                    this.resolvingInfo.IfIndex, this.resolvingInfo.ServiceName, this.resolvingInfo.Regtype, this.resolvingInfo.Domain,
                    eventManager);
            }
        }

        private void eventManager_ServiceResolved(DNSSDService service, DNSSDFlags flags, uint ifIndex, string fullname, string hostname, ushort port, TXTRecord record)
        {
            if (this.resolver != null)
            {
                this.resolver.Stop();
                this.resolver = null;
            }

            if (this.resolvingInfo != null)
            {
                this.resolvingInfo.Port = port;
                //this.resolvingInfo.Hostname = hostname;

                this.resolver = this.localService.QueryRecord(0, this.resolvingInfo.IfIndex, hostname, DNSSDRRType.kDNSSDType_A, DNSSDRRClass.kDNSSDClass_IN, eventManager);
            }
        }

        private void eventManager_QueryRecordAnswered(DNSSDService service, DNSSDFlags flags, uint ifIndex, string hostname, DNSSDRRType rrtype, DNSSDRRClass rrclass, object rdata, uint ttl)
        {
            if (this.resolver != null)
            {
                this.resolver.Stop();
                this.resolver = null;
            }

            if (this.resolvingInfo != null)
            {
                this.resolvingInfo.IPAddress = new IPAddress(BitConverter.ToUInt32((Byte[])rdata, 0));
                this.resolvingInfo.Resolved = true;

                OnResolved(this.resolvingInfo);

                this.resolvingInfo = null;
            }

            StartResolving();
        }

        private void eventManager_ServiceLost(DNSSDService browser, DNSSDFlags flags, uint ifIndex, string serviceName, string regtype, string domain)
        {
            var removedInfos = new List<ServiceInfo>(1);
            if (this.localServiceInfo != null)
            {
                if (ServiceInfo.Match(this.localServiceInfo, serviceName, regtype, domain))
                {
                    removedInfos.Add(this.localServiceInfo);
                    this.localServiceInfo = null;

                    if (this.localRegistrar != null)
                    {
                        this.localRegistrar.Stop();
                        this.localRegistrar = null;
                    }
                }
            }
            
            var remoteInfosToRemove = new List<ServiceInfo>(1);
            foreach (var info in this.remoteServiceInfos)
            {
                if (ServiceInfo.Match(info, serviceName, regtype, domain))
                {
                    remoteInfosToRemove.Add(info);
                }
            }
            foreach (var info in remoteInfosToRemove)
            {
                this.remoteServiceInfos.Remove(info);
                removedInfos.Add(info);
            }

            foreach (var info in removedInfos)
            {
                if (info == this.resolvingInfo)
                {
                    if (this.resolver != null)
                    {
                        this.resolver.Stop();
                        this.resolver = null;
                    }

                    this.resolvingInfo = null;
                }
            }

            // ReSharper disable BitwiseOperatorOnEnumWihtoutFlags
            if ((flags & DNSSDFlags.kDNSSDFlagsMoreComing) != DNSSDFlags.kDNSSDFlagsMoreComing)
            // ReSharper restore BitwiseOperatorOnEnumWihtoutFlags
            {
                if (this.resolvingInfo == null)
                {
                    StartResolving();
                }
            }
        }

        private static void eventManager_OperationFailed(DNSSDService service, DNSSDError error)
        {
            throw new CustomException<Communicator>("Operation Failed:" + error);
        }

        public IPEndPoint[] GetResolvedRemoteServiceEndPoints()
        {
            return GetResolvedServiceEndPoints(this.remoteServiceInfos);
        }

        private void OnResolved(ServiceInfo info)
        {
            foreach (var endPoint in GetResolvedServiceEndPoints(this.remoteServiceInfos.Intersect(new[] { info })))
            {
                this.newRemoteEndPointUpdates.OnNext(endPoint);
            }
        }

        private static IPEndPoint[] GetResolvedServiceEndPoints(IEnumerable<ServiceInfo> infos)
        {
            return infos
                .Where(x => x.Resolved)
                .Select(x => new IPEndPoint(x.IPAddress, x.Port.GetValueOrDefault()))
                .ToArray();
        }

        public void Send(string message, IPEndPoint endPoint)
        {
            Enforce.ArgumentNotEmptyOrNull(message);
            Enforce.ArgumentNotNull(endPoint);

            this.socket.SendTo(Encoding.ASCII.GetBytes(message), endPoint);
        }

        private static void OnReceivedFromSocket(IAsyncResult result)
        {
            var controller = (SocketController)result.AsyncState;
            EndPoint remoteEndPoint = new IPEndPoint(IPAddress.Any, 0);
            var read = controller.Socket.EndReceiveFrom(result, ref remoteEndPoint);
            if (read > 0)
            {
                var message = Encoding.ASCII.GetString(controller.Buffer, 0, read);
                controller.Invoke(message, (IPEndPoint)remoteEndPoint);
                
                EndPoint remoteEndPoint2 = new IPEndPoint(IPAddress.Any, 0); // create a new end point for better thread safety
                controller.Socket.BeginReceiveFrom(controller.Buffer, 0, controller.Buffer.Length, SocketFlags.None, ref remoteEndPoint2, OnReceivedFromSocket, controller);
            }
        }

        public void Dispose()
        {
            this.eventManager.ServiceRegistered -= eventManager_ServiceRegistered;
            this.eventManager.ServiceFound -= eventManager_ServiceFound;
            this.eventManager.ServiceLost -= eventManager_ServiceLost;
            this.eventManager.ServiceResolved -= eventManager_ServiceResolved;
            this.eventManager.QueryRecordAnswered -= eventManager_QueryRecordAnswered;
            this.eventManager.OperationFailed -= eventManager_OperationFailed;

            this.localService.Stop();

            if (this.localBrowser != null)
                this.localBrowser.Stop();

            if (this.localRegistrar != null)
                this.localRegistrar.Stop();

            if (this.resolver != null)
                this.resolver.Stop();

            this.socket.Shutdown(SocketShutdown.Both);
            this.socket.Close();

            this.newRemoteEndPointUpdates.Dispose();
        }
        #endregion

        #region inner classes
        public enum OperationMode
        {
            BrowseOnly,
            PublishOnly,
            PublishThenBrowse,
        }

        private class SocketController
        {
            #region fields
            private readonly Socket socket;
            private readonly bool enablePacketInformation;
            private readonly byte[] buffer;
            private readonly Dispatcher dispatcher;
            private readonly Delegate updater;
            #endregion

            #region properties
            public Socket Socket
            {
                get { return socket; }
            }

            public bool EnablePacketInformation
            {
                get { return enablePacketInformation; }
            }

            public byte[] Buffer
            {
                get { return buffer; }
            }
            #endregion

            #region constructors
            public SocketController(Socket socket, bool enablePacketInformation, byte[] buffer, Dispatcher dispatcher, Delegate updater)
            {
                this.socket = socket;
                this.enablePacketInformation = enablePacketInformation;
                this.buffer = buffer;
                this.dispatcher = dispatcher;
                this.updater = updater;
            }
            #endregion

            #region methods
            public void Invoke(string message, IPEndPoint remoteEndPoint)
            {
                if (this.enablePacketInformation)
                {
                    this.dispatcher.Invoke(this.updater, message, remoteEndPoint);
                }
                else
                {
                    this.dispatcher.Invoke(this.updater, message);
                }
            }
            #endregion
        }

        private class ServiceInfo
        {
            #region fields
            private readonly uint? ifIndex;
            private readonly string serviceName;
            private readonly string regtype;
            private readonly string domain;
            #endregion

            #region properties
            public uint IfIndex
            {
                get
                {
                    if (!ifIndex.HasValue)
                        throw new InvalidOperationException("IfIndex is not set");
                    
                    return ifIndex.Value;
                }
            }

            public string ServiceName
            {
                get { return serviceName; }
            }

            public string Regtype
            {
                get { return regtype; }
            }

            public string Domain
            {
                get { return domain; }
            }

            public bool Resolved { get; set; }

            public int? Port { get; set; }
            public IPAddress IPAddress { get; set; }
            #endregion

            #region constructors
            public ServiceInfo(uint? ifIndex, string serviceName, string regtype, string domain)
            {
                this.ifIndex = ifIndex;
                this.serviceName = serviceName;
                this.regtype = regtype;
                this.domain = domain;
            }
            #endregion

            #region methods
            public static bool Match(ServiceInfo info, string serviceName, string regtype, string domain)
            {
                return info.ServiceName.Equals(serviceName, StringComparison.OrdinalIgnoreCase)
                    && info.Regtype.Equals(regtype, StringComparison.OrdinalIgnoreCase)
                    && info.Domain.Equals(domain, StringComparison.OrdinalIgnoreCase);
            }
            #endregion
        }
        #endregion
    }
}
