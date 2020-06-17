import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { View, 
        NativeModules,
    StyleSheet, Image, Animated, findNodeHandle } from 'react-native';


import decorateMapComponent, {
  SUPPORTED,
  USES_DEFAULT_IMPLEMENTATION,
} from './decorateMapComponent';

const viewConfig = {
  uiViewClassName: 'AIR<provider>MapOverlay',
  validAttributes: {
    image: true,
  },
};

const propTypes = {
  ...View.propTypes,
  // A custom image to be used as overlay.
  //image: PropTypes.any.isRequired,
  image: PropTypes.any,
  imageList: PropTypes.any,
  // Top left and bottom right coordinates for the overlay
  bounds: PropTypes.arrayOf(PropTypes.array.isRequired).isRequired,
  /* Boolean to allow an overlay to be tappable and use the
   * onPress function
   */
  tappable: PropTypes.bool,
  // Callback that is called when the user presses on the overlay
  onPress: PropTypes.func,
  // The opacity of the overlay.
  opacity: PropTypes.number,
};

class MapOverlay extends Component {
    
  step(callback) {
    this._runCommand('step', [callback])
  }


  _getHandle() {
    return findNodeHandle(this.mapOverlay);

  }

  _runCommand(name, args) {
    switch (Platform.OS) {
        case 'android': 
            //return NativeModules.UIManager.dispatchViewManagerCommand(
            /*NativeModules.UIManager.dispatchViewManagerCommand(
                this._getHandle(),
                this.getUIManagerCommand(name),
                []
            );*/
            return NativeModules.AndroidOverlayModule.step(...args);
            
            //return NativeModules.AndroidOverlayModule.getIdx(...args);
        case 'ios':
            return this.getMapManagerCommand(name)(this._getHandle(), ...args);
        default:
            return Promise.reject(`Invalid platform was passed: ${Platform.OS}`);
    }
  }

  render() {
    let image;
    if (this.props.image) {
      if (
        typeof this.props.image.startsWith === 'function' &&
        this.props.image.startsWith('http')
      ) {
        image = this.props.image;
      } else {
        image = Image.resolveAssetSource(this.props.image) || {};
        image = image.uri;
      }
    }

    const AIRMapOverlay = this.getAirComponent();


    return (
      <AIRMapOverlay
          ref={ref => {
            this.mapOverlay = ref;
          }}
        {...this.props}
        image={image}
        style={[styles.overlay, this.props.style]}
      />
    );
  }
}

MapOverlay.propTypes = propTypes;
MapOverlay.viewConfig = viewConfig;
MapOverlay.defaultProps = {
  opacity: 1.0,
};

const styles = StyleSheet.create({
  overlay: {
    position: 'absolute',
    backgroundColor: 'transparent',
  },
});

MapOverlay.Animated = Animated.createAnimatedComponent(MapOverlay);

export default decorateMapComponent(MapOverlay, {
  componentType: 'Overlay',
  providers: {
    google: {
      ios: SUPPORTED,
      android: USES_DEFAULT_IMPLEMENTATION,
    },
  },
});
