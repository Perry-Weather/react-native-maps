#import "AIRMapOverlay.h"

#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTImageLoaderProtocol.h>
#import <React/RCTUtils.h>
#import <React/UIView+React.h>

@interface AIRMapOverlay()
@property (nonatomic, strong, readwrite) UIImage *overlayImage;
@property (nonatomic, strong, readwrite) NSMutableArray<UIImage *> *overlayImageList;

@end

@implementation AIRMapOverlay {
    RCTImageLoaderCancellationBlock _reloadImageCancellationBlock;
    CLLocationCoordinate2D _southWest;
    CLLocationCoordinate2D _northEast;
    MKMapRect _mapRect;
}

- (void) setOpacity:(float)opacity
 {
     _opacity = opacity;
     if (self.renderer)
     {
         self.renderer.alpha = _opacity;
     }
 }


- (void) fetchSingleImage: ( NSMutableArray<NSString*> * )remainingImgList forFirst:(Boolean)first
{
    __weak typeof(self) weakSelf = self;
    _reloadImageCancellationBlock = [[_bridge moduleForName:@"ImageLoader"] loadImageWithURLRequest:[RCTConvert NSURLRequest:[remainingImgList firstObject]]
                        size:weakSelf.bounds.size
                       scale:RCTScreenScale()
                     clipped:YES
                  resizeMode:RCTResizeModeCenter
               progressBlock:nil
            partialLoadBlock:nil
             completionBlock:^(NSError *error, UIImage *image) {
                 if (error) {
                     NSLog(@"%@", error);
                 }
                 dispatch_async(dispatch_get_main_queue(), ^{
                     NSLog(@">>> IMAGE: %@", image);
//                                                                             weakSelf.overlayImage = image;
                     if (weakSelf.overlayImageList == nil)
                     {
                         weakSelf.overlayImageList = [[NSMutableArray alloc]init];
                     }
                     [weakSelf.overlayImageList addObject:image];
                     
//                     if (weakSelf.imageList count)
                     [remainingImgList removeObjectAtIndex:0];
                     
                     
                     
                     [weakSelf createOverlayRendererIfPossible];
                     if (first == true)
                     {
                         [weakSelf update];
                     }
                     //Need to call this on the first image only.

                     
                     if  ([remainingImgList count] > 0)
                         [self fetchSingleImage:remainingImgList forFirst:false];
                 });
             }];

    
}


- (void) setImageList: (NSMutableArray<NSString *> *)imageList
{
    NSLog(@">>> SET_IMAGERC_LIST: %@", imageList);
    _imageList = imageList;
    [_overlayImageList removeAllObjects];
    
    if (_reloadImageCancellationBlock) {
        _reloadImageCancellationBlock();
        _reloadImageCancellationBlock = nil;
    }
//    weakSelf.overlayImageList = [[NSMutableArray alloc]init];
    
    NSMutableArray<NSString *>* list = [NSMutableArray alloc];
    list = [list initWithArray:imageList copyItems:true];
//    __weak typeof(self) weakSelf = self;
    //this probably won't be in order.
    [self fetchSingleImage: list forFirst:true];
//    for (NSString* img in imageList) {
//        _reloadImageCancellationBlock = [[_bridge moduleForName:@"ImageLoader"] loadImageWithURLRequest:[RCTConvert NSURLRequest:img]
//                                                                                size:weakSelf.bounds.size
//                                                                               scale:RCTScreenScale()
//                                                                             clipped:YES
//                                                                          resizeMode:RCTResizeModeCenter
//                                                                       progressBlock:nil
//                                                                    partialLoadBlock:nil
//                                                                     completionBlock:^(NSError *error, UIImage *image) {
//                                                                         if (error) {
//                                                                             NSLog(@"%@", error);
//                                                                         }
//                                                                         dispatch_async(dispatch_get_main_queue(), ^{
//                                                                             NSLog(@">>> IMAGE: %@", image);
//                                                                             if (weakSelf.overlayImageList == nil)
//                                                                             {
//                                                                                 weakSelf.overlayImageList = [[NSMutableArray alloc]init];
//                                                                             }
//                                                                             [weakSelf.overlayImageList addObject:image];
//
//                                                                             [weakSelf createOverlayRendererIfPossible];
//                                                                             [weakSelf update];
//                                                                         });
//                                                                     }];
//    }
//
//
//    __weak typeof(self) weakSelf = self;
//    _reloadImageCancellationBlock = [[_bridge moduleForName:@"ImageLoader"] loadImageWithURLRequest:[RCTConvert NSURLRequest:_imageSrc]
//                                                                            size:weakSelf.bounds.size
//                                                                           scale:RCTScreenScale()
//                                                                         clipped:YES
//                                                                      resizeMode:RCTResizeModeCenter
//                                                                   progressBlock:nil
//                                                                partialLoadBlock:nil
//                                                                 completionBlock:^(NSError *error, UIImage *image) {
//                                                                     if (error) {
//                                                                         NSLog(@"%@", error);
//                                                                     }
//                                                                     dispatch_async(dispatch_get_main_queue(), ^{
//                                                                         NSLog(@">>> IMAGE: %@", image);
//                                                                         weakSelf.overlayImage = image;
//                                                                         [weakSelf createOverlayRendererIfPossible];
//                                                                         [weakSelf update];
//                                                                     });
//                                                                 }];
     
}

- (void)setImageSrc:(NSString *)imageSrc
{
    NSLog(@">>> SET IMAGESRC: %@", imageSrc);
    _imageSrc = imageSrc;

    if (_reloadImageCancellationBlock) {
        _reloadImageCancellationBlock();
        _reloadImageCancellationBlock = nil;
    }
    __weak typeof(self) weakSelf = self;
    _reloadImageCancellationBlock = [[_bridge moduleForName:@"ImageLoader"] loadImageWithURLRequest:[RCTConvert NSURLRequest:_imageSrc]
                                                                            size:weakSelf.bounds.size
                                                                           scale:RCTScreenScale()
                                                                         clipped:YES
                                                                      resizeMode:RCTResizeModeCenter
                                                                   progressBlock:nil
                                                                partialLoadBlock:nil
                                                                 completionBlock:^(NSError *error, UIImage *image) {
                                                                     if (error) {
                                                                         NSLog(@"%@", error);
                                                                     }
                                                                     dispatch_async(dispatch_get_main_queue(), ^{
                                                                         NSLog(@">>> IMAGE: %@", image);
                                                                         weakSelf.overlayImage = image;
                                                                         [weakSelf createOverlayRendererIfPossible];
//                                                                         [weakSelf update];
                                                                     });
                                                                 }];
}

- (void)setBoundsRect:(NSArray *)boundsRect {
    _boundsRect = boundsRect;

    _southWest = CLLocationCoordinate2DMake([boundsRect[0][0] doubleValue], [boundsRect[0][1] doubleValue]);
    _northEast = CLLocationCoordinate2DMake([boundsRect[1][0] doubleValue], [boundsRect[1][1] doubleValue]);

    MKMapPoint southWest = MKMapPointForCoordinate(_southWest);
    MKMapPoint northEast = MKMapPointForCoordinate(_northEast);

    _mapRect = MKMapRectMake(southWest.x, northEast.y, ABS(northEast.x - southWest.x), ABS(northEast.y - southWest.y));

    [self update];
}

- (void)createOverlayRendererIfPossible
{
    if (MKMapRectIsEmpty(_mapRect) || (!self.overlayImage && !self.overlayImageList) || self.renderer ) return;
//    if (MKMapRectIsEmpty(_mapRect) || (!self.overlayImage && (!self.overlayImageList && self.overlayImageList.count < 1))) return;
    
    __weak typeof(self) weakSelf = self;
    self.renderer = [[AIRMapOverlayRenderer alloc] initWithOverlay:weakSelf];
//    [NSTimer scheduledTimerWithTimeInterval:(0.5) target:self selector:@selector(onTimer) userInfo:nil repeats:YES];
    
    if (self.opacity) {
        self.renderer.alpha = self.opacity;
    }
}

-(void)onTimer {
//    [self update];
//    [self draw];
    [self IncreaseIndex];
    [[self renderer] setNeedsDisplay];
//    [self setNeedsDisplay];
}

- (void)update
{
    if (!_renderer) return;

    if (_map == nil) return;
    [_map removeOverlay:self];
    [_map addOverlay:self];
}


#pragma mark MKOverlay implementation

- (CLLocationCoordinate2D)coordinate
{
    return MKCoordinateForMapPoint(MKMapPointMake(MKMapRectGetMidX(_mapRect), MKMapRectGetMidY(_mapRect)));
}

- (MKMapRect)boundingMapRect
{
    return _mapRect;
}

- (BOOL)intersectsMapRect:(MKMapRect)mapRect
{
    return MKMapRectIntersectsRect(_mapRect, mapRect);
}

- (BOOL)canReplaceMapContent
{
    return NO;
}

- (NSInteger)IncreaseIndex
{
    self.imageIndex = self.imageIndex+1;
    return self.imageIndex;
}

@end

