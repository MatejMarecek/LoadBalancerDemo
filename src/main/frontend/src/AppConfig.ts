export {AppConfig}


class AppConfig {
  /**
   * Change this property depending on the mode of the application.
   * If you want to run the app in a dev mode, set the value to false.
   *
   * This way, the web sockets connect to your local server (assuming port 8080).
   * HTTP calls will be redirected to the port 8080 as well.
   *   - @see "proxy" : "http://localhost:8080/" in the package.json
   */
  public static readonly isProduction = true;

  public static getWebSocketUrl() {
    return AppConfig.isProduction ? '/loadbalancer-websocket' :
                                    'http://localhost:8080/loadbalancer-websocket';
  }

  public static getProviderConfigUrl() : string {
    return '/provider/default/configuration';
  }

  public static getLoadBalancerConfigUrl() : string {
    return '/loadbalancer/configuration';
  }
}