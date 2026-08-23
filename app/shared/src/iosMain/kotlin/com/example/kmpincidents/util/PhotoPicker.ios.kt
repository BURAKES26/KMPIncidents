package com.example.kmpincidents.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDate
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.darwin.NSObject

actual class PhotoPicker internal constructor(
    private val openCamera: () -> Unit,
    private val openGallery: () -> Unit,
) {
    actual fun pickFromCamera() {
        openCamera()
    }

    actual fun pickFromGallery() {
        openGallery()
    }
}

@Composable
actual fun rememberPhotoPicker(onPhotoPicked: (String) -> Unit): PhotoPicker {
    return remember(onPhotoPicked) {
        PhotoPicker(
            openCamera = {
                presentImagePicker(
                    sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera,
                    onPhotoPicked = onPhotoPicked,
                )
            },
            openGallery = {
                presentImagePicker(
                    sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary,
                    onPhotoPicked = onPhotoPicked,
                )
            },
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun presentImagePicker(
    sourceType: UIImagePickerControllerSourceType,
    onPhotoPicked: (String) -> Unit,
) {
    val presenter = topViewController() ?: return

    val resolvedSourceType =
        if (UIImagePickerController.isSourceTypeAvailable(sourceType)) {
            sourceType
        } else if (
            sourceType == UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera &&
            UIImagePickerController.isSourceTypeAvailable(
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            )
        ) {
            // Simulator / devices without camera fall back to the photo library.
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        } else {
            return
        }

    val picker = UIImagePickerController()
    picker.sourceType = resolvedSourceType
    picker.allowsEditing = false

    val delegate = ImagePickerDelegate(
        onPhotoPicked = onPhotoPicked,
        onFinished = {
            picker.dismissViewControllerAnimated(true, completion = null)
            ImagePickerDelegateHolder.current = null
        },
    )
    // Retain the delegate for the lifetime of the picker presentation.
    ImagePickerDelegateHolder.current = delegate
    picker.delegate = delegate

    presenter.presentViewController(picker, animated = true, completion = null)
}

@Suppress("DEPRECATION")
private fun topViewController(): UIViewController? {
    val application = UIApplication.sharedApplication
    val keyWindow = application.keyWindow
        ?: application.windows
            .mapNotNull { it as? UIWindow }
            .firstOrNull { it.isKeyWindow }
        ?: application.windows.mapNotNull { it as? UIWindow }.firstOrNull()

    var controller = keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller?.presentedViewController
    }
    return controller
}

private object ImagePickerDelegateHolder {
    var current: ImagePickerDelegate? = null
}

@OptIn(ExperimentalForeignApi::class)
private class ImagePickerDelegate(
    private val onPhotoPicked: (String) -> Unit,
    private val onFinished: () -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val path = image?.let { saveImageToTempFile(it) }
        if (path != null) {
            onPhotoPicked(path)
        }
        onFinished()
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        onFinished()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun saveImageToTempFile(image: UIImage): String? {
    val data = UIImageJPEGRepresentation(image, 0.9) ?: return null
    val fileName = "kmpincidents_photo_${NSDate().timeIntervalSince1970}.jpg"
    val path = NSTemporaryDirectory() + fileName
    return if (data.writeToFile(path, atomically = true)) path else null
}
