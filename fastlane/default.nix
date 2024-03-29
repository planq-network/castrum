{ stdenv, lib, bundlerEnv, ruby, makeWrapper }:

let
  fastlane = stdenv.mkDerivation rec {
    name = "${pname}-${version}";
    pname = "fastlane";
    version = (import ./gemset.nix).fastlane.version;

    nativeBuildInputs = [ makeWrapper ];

    env = bundlerEnv {
      name = "${name}-gems";
      inherit pname ruby;
      gemdir = ./.;
    };

    phases = [ "installPhase" ];

    installPhase = ''
      mkdir -p $out/bin
      makeWrapper ${env}/bin/fastlane $out/bin/fastlane \
        --set FASTLANE_SKIP_UPDATE_CHECK 1
    '';

    shellHook = ''
      [ -z "$STATUS_MOBILE_HOME" ] && echo "STATUS_MOBILE_HOME is empty!" && exit 1

      export FASTLANE_PLUGINFILE_PATH=$STATUS_MOBILE_HOME/fastlane/Pluginfile
    '';

    meta = with lib; {
      description     = "A tool to automate building and releasing iOS and Android apps";
      longDescription = "fastlane is a tool for iOS and Android developers to automate tedious tasks like generating screenshots, dealing with provisioning profiles, and releasing your application.";
      homepage        = https://github.com/fastlane/fastlane;
      license         = licenses.mit;
      maintainers     = with maintainers; [
        peterromfeldhk
      ];
    };
  };

in fastlane
