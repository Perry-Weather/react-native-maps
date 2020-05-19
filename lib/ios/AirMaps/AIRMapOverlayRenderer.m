#import "AIRMapOverlayRenderer.h"
#import "AIRMapOverlay.h"

@implementation AIRMapOverlayRenderer

- (void)drawMapRect:(MKMapRect)mapRect zoomScale:(MKZoomScale)zoomScale inContext:(CGContextRef)context {
//    UIImage *image = [[(AIRMapOverlay *)self.overlay overlayImageList] firstObject];
    NSInteger index = [(AIRMapOverlay *) self.overlay imageIndex];
    NSInteger maxIndex = [[(AIRMapOverlay *) self.overlay overlayImageList] count];
    UIImage *image;
    NSLog(@">>> index: %d", (long)index);
    if ((index) < maxIndex)
         image = [[(AIRMapOverlay *)self.overlay overlayImageList] objectAtIndex:index];
    else {
         ((AIRMapOverlay *) self.overlay).imageIndex = 0;
         image = [[(AIRMapOverlay *)self.overlay overlayImageList] objectAtIndex:0];
    }
        


//   UIImage *image = [(AIRMapOverlay *)self.overlay overlayImage];
    
    
    CGContextSaveGState(context);
    
    CGImageRef imageReference = image.CGImage;
    
    MKMapRect theMapRect = [self.overlay boundingMapRect];
    CGRect theRect = [self rectForMapRect:theMapRect];
    
    CGContextRotateCTM(context, M_PI);
    CGContextScaleCTM(context, -1.0, 1.0);
    CGContextTranslateCTM(context, 0.0, -theRect.size.height);
    CGContextAddRect(context, theRect);
    CGContextDrawImage(context, theRect, imageReference);
    
    CGContextRestoreGState(context);
}

- (BOOL)canDrawMapRect:(MKMapRect)mapRect zoomScale:(MKZoomScale)zoomScale {
    return [(AIRMapOverlay *)self.overlay overlayImageList] != nil;
//    return [(AIRMapOverlay *)self.overlay overlayImage] != nil;

}


@end

