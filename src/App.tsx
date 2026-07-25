import React, { useState } from 'react';
import { Shield, ShieldAlert, Globe, Server, Activity } from 'lucide-react';
import { motion } from 'motion/react';

export default function App() {
  const [isConnected, setIsConnected] = useState(false);
  const [isConnecting, setIsConnecting] = useState(false);

  const handleConnect = () => {
    if (isConnected) {
      setIsConnected(false);
      return;
    }
    
    setIsConnecting(true);
    setTimeout(() => {
      setIsConnecting(false);
      setIsConnected(true);
    }, 1500);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
      <div className="max-w-md w-full bg-white rounded-3xl shadow-xl overflow-hidden border border-gray-100">
        
        {/* Header */}
        <div className="p-6 bg-slate-900 text-white flex justify-between items-center">
          <div className="flex items-center gap-2">
            <Globe className="w-5 h-5 text-blue-400" />
            <h1 className="font-semibold text-lg tracking-tight">VPN-Project</h1>
          </div>
          <div className="flex items-center gap-2">
            <span className="relative flex h-3 w-3">
              {isConnected && <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>}
              <span className={`relative inline-flex rounded-full h-3 w-3 ${isConnected ? 'bg-emerald-500' : 'bg-rose-500'}`}></span>
            </span>
            <span className="text-sm font-medium text-slate-300">
              {isConnected ? 'Secured' : 'Unprotected'}
            </span>
          </div>
        </div>

        {/* Main Content */}
        <div className="p-8 flex flex-col items-center">
          
          <motion.div 
            animate={{ 
              scale: isConnected ? [1, 1.05, 1] : 1,
              color: isConnected ? '#10b981' : isConnecting ? '#3b82f6' : '#94a3b8'
            }}
            transition={{ duration: 1, repeat: isConnected ? Infinity : 0 }}
            className="mb-8"
          >
            {isConnected ? (
              <Shield className="w-24 h-24 text-emerald-500 drop-shadow-md" />
            ) : (
              <ShieldAlert className="w-24 h-24 text-slate-400" />
            )}
          </motion.div>
          
          <div className="mb-10 w-full">
            <div className="flex items-center justify-between p-4 bg-slate-50 rounded-xl border border-slate-100">
              <div className="flex items-center gap-3">
                <div className="p-2 bg-blue-100 text-blue-600 rounded-lg">
                  <Server className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-sm text-slate-500 font-medium">Virtual Location</p>
                  <p className="font-semibold text-slate-800">
                    {isConnected ? 'Frankfurt, Germany' : 'Automatic'}
                  </p>
                </div>
              </div>
            </div>
            
            {isConnected && (
              <motion.div 
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                className="mt-4 flex items-center justify-between p-4 bg-emerald-50 rounded-xl border border-emerald-100"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 bg-emerald-100 text-emerald-600 rounded-lg">
                    <Activity className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-sm text-emerald-600 font-medium">Connection active</p>
                    <p className="font-semibold text-emerald-800 text-sm">
                      Your IP is hidden
                    </p>
                  </div>
                </div>
              </motion.div>
            )}
          </div>

          <button
            onClick={handleConnect}
            disabled={isConnecting}
            className={`w-full py-4 rounded-2xl font-semibold text-lg transition-all duration-200 flex items-center justify-center gap-2 ${
              isConnected 
                ? 'bg-slate-100 text-slate-700 hover:bg-slate-200' 
                : isConnecting
                ? 'bg-blue-400 text-white cursor-wait'
                : 'bg-blue-600 text-white hover:bg-blue-700 shadow-lg shadow-blue-200'
            }`}
          >
            {isConnecting ? (
              <span className="flex items-center gap-2">
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                Connecting...
              </span>
            ) : isConnected ? (
              'Disconnect'
            ) : (
              'Tap to Connect'
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
