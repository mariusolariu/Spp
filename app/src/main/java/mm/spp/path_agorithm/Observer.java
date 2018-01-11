package mm.spp.path_agorithm;

import java.util.List;

/**
 * Created by molariu
 * The activities implementing this interface draw the paths found on map
 */

public interface Observer {
  void findingRoutesStarted();

  void routesFound(List<Route> routes);
}
