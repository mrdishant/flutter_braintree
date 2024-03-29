package com.example.flutter_braintree;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.braintreepayments.api.PayPalVaultRequest;

import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class FlutterBraintreePlugin implements FlutterPlugin, ActivityAware, MethodCallHandler, ActivityResultListener {
    private static final int CUSTOM_ACTIVITY_REQUEST_CODE = 0x420;

    private Activity activity;
    private Result activeResult;

//    private FlutterBraintreeDropIn dropIn;

    public static void registerWith(Registrar registrar) {
//        FlutterBraintreeDropIn.registerWith(registrar);
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_braintree.custom");
        FlutterBraintreePlugin plugin = new FlutterBraintreePlugin();
        plugin.activity = registrar.activity();
        registrar.addActivityResultListener(plugin);
        channel.setMethodCallHandler(plugin);
    }

    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        final MethodChannel channel = new MethodChannel(binding.getBinaryMessenger(), "flutter_braintree.custom");
        channel.setMethodCallHandler(this);

//        dropIn = new FlutterBraintreeDropIn();
//        dropIn.onAttachedToEngine(binding);
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
//        dropIn.onDetachedFromEngine(binding);
//        dropIn = null;
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
//        dropIn.onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
//        dropIn.onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
//        dropIn.onReattachedToActivityForConfigChanges(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
//        dropIn.onDetachedFromActivity();
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (activeResult != null) {
            result.error("already_running", "Cannot launch another custom activity while one is already running.", null);
            return;
        }
        activeResult = result;

        if (call.method.equals("tokenizeCreditCard")) {
            String authorization = call.argument("authorization");
            Intent intent = new Intent(activity, FlutterBraintreeCustom.class);
            intent.putExtra("type", "tokenizeCreditCard");
            intent.putExtra("authorization", (String) call.argument("authorization"));
            assert (call.argument("request") instanceof Map);
            Map request = (Map) call.argument("request");
            intent.putExtra("cardNumber", (String) request.get("cardNumber"));
            intent.putExtra("expirationMonth", (String) request.get("expirationMonth"));
            intent.putExtra("expirationYear", (String) request.get("expirationYear"));
            intent.putExtra("cvv", (String) request.get("cvv"));
            activity.startActivityForResult(intent, CUSTOM_ACTIVITY_REQUEST_CODE);
        } else if (call.method.equals("requestPaypalNonce")) {
            String authorization = call.argument("authorization");
            Intent intent = new Intent(activity, FlutterBraintreeCustom.class);
            intent.putExtra("type", "requestPaypalNonce");
            intent.putExtra("authorization", (String) call.argument("authorization"));
            assert (call.argument("request") instanceof Map);
            Map request = (Map) call.argument("request");
            intent.putExtra("amount", (String) request.get("amount"));
            intent.putExtra("currencyCode", (String) request.get("currencyCode"));
            intent.putExtra("displayName", (String) request.get("displayName"));
            intent.putExtra("billingAgreementDescription", (String) request.get("billingAgreementDescription"));
            activity.startActivityForResult(intent, CUSTOM_ACTIVITY_REQUEST_CODE);
//            String amount = (String) request.get("amount");
//            if(amount ==null){
//                paypalVaultFlow(call);
//            }else{
//                paypalCheckOutFlow(call);
//            }

        } else if (call.method.equals("isApplePayAvailable")) {
            result.success(false);
        } else if (call.method.equals("isGooglePayAvailable")) {

            Intent intent = new Intent(activity, FlutterBraintreeCustom.class);
            intent.putExtra("type", "isGooglePayAvailable");
            intent.putExtra("authorization", (String) call.argument("authorization"));
            activity.startActivityForResult(intent, CUSTOM_ACTIVITY_REQUEST_CODE);
        }  else if (call.method.equals("collectDeviceData")) {

            Intent intent = new Intent(activity, FlutterBraintreeCustom.class);
            intent.putExtra("type", "collectDeviceData");
            intent.putExtra("authorization", (String) call.argument("authorization"));
            activity.startActivityForResult(intent, CUSTOM_ACTIVITY_REQUEST_CODE);
        } else if (call.method.equals("payWithGooglePay")) {

            Intent intent = new Intent(activity, FlutterBraintreeCustom.class);
            intent.putExtra("type", "payWithGooglePay");
            intent.putExtra("authorization", (String) call.argument("authorization"));
            intent.putExtra("testing", (boolean) call.argument("testing"));
            intent.putExtra("label", (String) call.argument("label"));
            intent.putExtra("currencyCode", (String) call.argument("currencyCode"));
            intent.putExtra("total", (String) call.argument("total"));
            activity.startActivityForResult(intent, CUSTOM_ACTIVITY_REQUEST_CODE);
        } else {
            result.notImplemented();
            activeResult = null;
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {

        if (activeResult == null)
            return false;

        if (requestCode == CUSTOM_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String type = data.getStringExtra("type");
                switch (type) {
                    case "paymentMethodNonce":
                        activeResult.success(data.getSerializableExtra("paymentMethodNonce"));
                        break;
                    case "isGooglePayAvailable":
                        activeResult.success(data.getBooleanExtra("result", false));
                        break;
                    case "collectDeviceData":
                        activeResult.success(data.getStringExtra("result"));
                        break;
                    default:
                        Exception error = new Exception("Invalid activity result type.");
                        activeResult.error("error", error.getMessage(), null);
                        break;
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                activeResult.success(null);
            } else {
                Exception error = (Exception) data.getSerializableExtra("error");
                activeResult.error("error", error.getMessage(), null);
            }
            activeResult = null;
            return true;
        }
        return false;
    }
}
