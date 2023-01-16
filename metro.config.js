/**
 * Metro configuration for React Native
 * https://github.com/facebook/react-native
 *
 * @format
 */
const defaultResolver = require('metro-resolver').resolve;

module.exports = {
    transformer: {
        getTransformOptions: async () => ({
            transform: {
                experimentalImportSupport: false,
                inlineRequires: true,
            },
        }),
    },
    resolver: {
        extraNodeModules: require('node-libs-react-native'),
        resolveRequest: (context, moduleName, platform, realModuleName) => {
        if (moduleName.startsWith('@ledgerhq/devices') || moduleName.startsWith('@ledgerhq/cryptoassets') ) {
          return {
            filePath: require.resolve(moduleName, {
              paths: [
                require('path').dirname(context.originModulePath),
              ],
            }),
            type: 'sourceFile',
          };
        }
        return defaultResolver(
                {
                  ...context,
                  resolveRequest: null,
                }, moduleName, platform, realModuleName);
        },
    },
};
