#import "AIRMapOverlayManager.h"

#import <React/RCTConvert+CoreLocation.h>
#import <React/RCTUIManager.h>
#import <React/UIView+React.h>
#import "AIRMapOverlay.h"

@interface AIRMapOverlayManager () <MKMapViewDelegate>

@end

@implementation AIRMapOverlayManager

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(step:(nonnull NSNumber *)reactTag :(RCTResponseSenderBlock)callback)
{
    NSLog(@"stepping in native");
    
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, UIView *> *viewRegistry) {
         id view = viewRegistry[reactTag];
         if (![view isKindOfClass:[AIRMapOverlay class]]) {
             RCTLogError(@"Invalid view returned from registry, expecting AIRMap, got: %@", view);
         } else {
             NSLog(@"Overlay exists! Might be able to get renderer here.");
//             [(AIRMapOverlay *) view renderer];
             AIRMapOverlay* overlay =  view;
            
             
             NSInteger idx = [overlay IncreaseIndex];
//             NSInteger idx = [overlay imageIndex];
            
             callback(@[[NSNull null], @(idx)]);
             [[overlay renderer] setNeedsDisplay];
            


         }
     }];
}

//RCT_EXPORT_METHOD(setImageIndex)


- (UIView *)view
{
    AIRMapOverlay *overlay = [AIRMapOverlay new];
    overlay.bridge = self.bridge;
    return overlay;
}

RCT_REMAP_VIEW_PROPERTY(bounds, boundsRect, NSArray)
RCT_REMAP_VIEW_PROPERTY(image, imageSrc, NSString)
RCT_REMAP_VIEW_PROPERTY(imageList, imageList, NSArray<NSString*>)
RCT_EXPORT_VIEW_PROPERTY(opacity, float)


@end

