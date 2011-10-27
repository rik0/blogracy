package it.unipr.aotlab.userRss.network;

import it.unipr.aotlab.userRss.errors.NetworkConfigurationError;

class NetworkManager {
	private static Network currentNetwork = null;
	private static String loadedNetworkClassName = null;
	
	public static void configureNetwork(String networkClassName, Object... params) throws NetworkConfigurationError {
		if(currentNetwork == null) {
            loadNetworkImplementation(networkClassName);
            currentNetwork.initialize(params);
		} else if (loadedNetworkClassName.equals(networkClassName)) {
			return;
		} else {
			throw new NetworkConfigurationError("Already configured network.");
		}
	}
    
    private static void loadNetworkImplementation(String networkImplementation) throws NetworkConfigurationError {
        Class<Network> loadedImplementation;
        try {
            loadedImplementation = (Class<Network>) Class.forName(networkImplementation);
            Network instance = loadedImplementation.newInstance();
            loadedNetworkClassName = networkImplementation;
            currentNetwork = instance;
        } catch (Exception e) {
            throw new NetworkConfigurationError(e);
        }
    }


    Network getNetwork() throws NetworkConfigurationError {
        if(currentNetwork != null) {
		    return currentNetwork;
        } else {
            throw new NetworkConfigurationError("Network not configured.");
        }
	}
}