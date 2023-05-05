// global-polyfills.js
import { TextEncoder, TextDecoder } from 'text-encoding';
import { AppState } from 'react-native';
//import typedarray from 'typedarray';

// Replace the global TextEncoder and TextDecoder with custom implementations
global.TextEncoder = global.TextEncoder || TextEncoder;
global.TextDecoder = global.TextDecoder || TextDecoder;
//global.Uint8Array = typedarray.Uint8Array;





const browserIdlePolyfill = {
  idle: {
    onStateChanged: {
      addListener: (callback) => {
        const handleAppStateChange = (nextAppState) => {
          if (nextAppState === 'active') {
            callback('active');
          } else if (nextAppState === 'background' || nextAppState === 'inactive') {
            callback('idle');
          }
        };

        AppState.addEventListener('change', handleAppStateChange);
        return () => {
          AppState.removeEventListener('change', handleAppStateChange);
        };
      },
    },
  },
};


global.browser = browserIdlePolyfill;