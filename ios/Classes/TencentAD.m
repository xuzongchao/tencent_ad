#import "TencentAD.h"
#if __has_include(<tencent_ad/tencent_ad-Swift.h>)
#import <tencent_ad/tencent_ad-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "tencent_ad-Swift.h"
#endif

@implementation TencentAD
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftTencentAD registerWithRegistrar:registrar];
}
@end
