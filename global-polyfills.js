// global-polyfills.js
import { TextEncoder, TextDecoder } from 'text-encoding';

// Replace the global TextEncoder and TextDecoder with custom implementations
global.TextEncoder = global.TextEncoder || TextEncoder;
global.TextDecoder = global.TextDecoder || TextDecoder;
