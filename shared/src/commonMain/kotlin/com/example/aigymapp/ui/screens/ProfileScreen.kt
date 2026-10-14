package com.example.aigymapp.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aigymapp.ui.components.topRule
import com.example.aigymapp.ui.theme.ModernistDark as MD
import com.example.aigymapp.ui.viewmodels.ProfileViewModel
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
import com.preat.peekaboo.ui.camera.PeekabooCamera
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Profile Screen — Modernist Dark: flat rows and hairline dividers instead
 * of rounded "kutucuk" cards, matching Home / Antrenman / İlerleme.
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state = viewModel.state

    // Bumped once per successful save so PersonalInfoSection's rows know a
    // field just changed and should run their highlight animation.
    var saveTrigger by remember { mutableStateOf(0) }

    // Avatar photo flow: camera button opens a small chooser, which routes
    // into either the system gallery picker or our own full-screen camera.
    var showAvatarChooser by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    val pickerScope = rememberCoroutineScope()
    val galleryPicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = pickerScope,
        onResult = { images -> images.firstOrNull()?.let { viewModel.updateAvatar(it) } }
    )

    Scaffold(
        modifier = modifier,
        containerColor = MD.Bg
    ) { paddingValues ->
        // Spring overscroll state — kept as-is, unrelated to the card styling.
        var dragOffset by remember { mutableStateOf(0f) }
        val springOffset by animateFloatAsState(
            targetValue = dragOffset,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessHigh
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MD.Bg)
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = { dragOffset = 0f },
                        onDragCancel = { dragOffset = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            // Tight rubber-band resistance (Apple-like)
                            dragOffset = (dragOffset + dragAmount * 0.2f).coerceIn(-60f, 60f)
                        }
                    )
                }
                .graphicsLayer {
                    translationY = springOffset
                }
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 22.dp, end = 22.dp, top = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(2.dp, MD.BorderStrong)
                        .bouncyClickable(pressedScale = 0.88f, onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = MD.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            StaggeredSection(index = 0) {
                ProfileHeaderSection(
                    name = state.name,
                    avatarBytes = state.avatarBytes,
                    onCameraClick = { showAvatarChooser = true }
                )
            }

            StaggeredSection(index = 1) {
                StatsSummaryStrip()
            }

            PersonalInfoSection(
                height = state.height,
                weight = state.weight,
                fat = state.fat,
                age = state.age,
                gender = state.gender,
                saveTrigger = saveTrigger,
                onEditClick = { viewModel.toggleEditMode() }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Edit Profile Dialog
        if (state.isEditing) {
            EditProfileDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.toggleEditMode() },
                onSaved = { saveTrigger++ }
            )
        }

        // Avatar seçici — "DÜZENLE" sayfasıyla aynı ModalBottomSheet eğrisiyle açılır.
        if (showAvatarChooser) {
            AvatarChooserDialog(
                onDismiss = { showAvatarChooser = false },
                onPickGallery = {
                    showAvatarChooser = false
                    galleryPicker.launch()
                },
                onPickCamera = {
                    showAvatarChooser = false
                    showCamera = true
                }
            )
        }

        // Kamera yakalama overlay'i — kendi kepenk/kapat düğmelerimizle.
        if (showCamera) {
            AvatarCameraCapture(
                onCapture = { bytes ->
                    bytes?.let { viewModel.updateAvatar(it) }
                    showCamera = false
                },
                onClose = { showCamera = false }
            )
        }
    }
}

/**
 * Avatar square + name + email, bottom-aligned, matching the redesign's
 * Ana Sayfa/Antrenman header language.
 */
@Composable
private fun ProfileHeaderSection(
    name: String,
    avatarBytes: ByteArray?,
    onCameraClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier.size(92.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, MD.BorderStrong),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = avatarBytes,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                    label = "profileAvatar"
                ) { bytes ->
                    if (bytes != null) {
                        Image(
                            bitmap = remember(bytes) { bytes.toImageBitmap() },
                            contentDescription = "Profil Resmi",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil Resmi",
                            tint = MD.TextFaint,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(26.dp)
                    .background(MD.Lime)
                    .bouncyClickable(pressedScale = 0.82f, onClick = onCameraClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Fotoğrafı Değiştir",
                    tint = MD.OnLime,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 24.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp,
                color = MD.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = "user@example.com",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MD.TextMuted
            )
        }
    }
}

/**
 * Antrenman / Seri / Aktif Gün — flush 3-column strip.
 */
@Composable
private fun StatsSummaryStrip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .topRule(2.dp, MD.BorderStrong)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileStatColumn(value = "0", label = "ANTRENMAN", modifier = Modifier.weight(1f))
        Box(modifier = Modifier.width(1.dp).height(40.dp).background(MD.BorderWeak))
        ProfileStatColumn(value = "0", label = "SERİ", modifier = Modifier.weight(1f))
        Box(modifier = Modifier.width(1.dp).height(40.dp).background(MD.BorderWeak))
        ProfileStatColumn(value = "0", label = "AKTİF GÜN", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ProfileStatColumn(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (slideInVertically(tween(160)) { h -> h / 2 } + fadeIn(tween(160))) togetherWith
                    (slideOutVertically(tween(160)) { h -> -h / 2 } + fadeOut(tween(160)))
            },
            label = "profileStatValue"
        ) { animatedValue ->
            Text(
                text = animatedValue,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = MD.Lime,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(0.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            letterSpacing = 1.3.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MD.TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Personal info as a flat, divider-separated list instead of a 2×2 grid of
 * rounded cards.
 */
@Composable
private fun PersonalInfoSection(
    height: String,
    weight: String,
    fat: String,
    age: String,
    gender: String,
    saveTrigger: Int,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .topRule(2.dp, MD.BorderStrong)
            .padding(horizontal = 22.dp, vertical = 16.dp)
    ) {
        StaggeredSection(index = 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KİŞİSEL BİLGİLER",
                    fontSize = 9.sp,
                    letterSpacing = 1.6.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.TextMuted
                )
                Text(
                    text = "DÜZENLE",
                    fontSize = 9.sp,
                    letterSpacing = 1.3.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.Lime,
                    modifier = Modifier.bouncyClickable(onClick = onEditClick)
                )
            }
        }

        Column(modifier = Modifier.padding(top = 4.dp)) {
            StaggeredSection(index = 3) {
                InfoRow(
                    label = "Boy", rawValue = height, unit = "CM", showDivider = false, onClick = onEditClick,
                    saveTrigger = saveTrigger, highlightIndex = 0
                )
            }
            StaggeredSection(index = 4) {
                InfoRow(
                    label = "Kilo", rawValue = weight, unit = "KG", onClick = onEditClick,
                    saveTrigger = saveTrigger, highlightIndex = 1
                )
            }
            StaggeredSection(index = 5) {
                InfoRow(
                    label = "Yağ Oranı", rawValue = fat, unit = null, isPercent = true, onClick = onEditClick,
                    saveTrigger = saveTrigger, highlightIndex = 2
                )
            }
            StaggeredSection(index = 6) {
                InfoRow(
                    label = "Yaş", rawValue = age, unit = null, onClick = onEditClick,
                    saveTrigger = saveTrigger, highlightIndex = 3
                )
            }
            StaggeredSection(index = 7) {
                InfoRow(label = "Cinsiyet", rawValue = gender, unit = null, onClick = onEditClick)
            }
        }
    }
}

/**
 * Profil sekmesine giriş animasyonu — üç üst bölüm (başlık, istatistik
 * şeridi, kişisel bilgiler bölümü) ve içindeki beş bilgi satırı tek bir
 * sıralı kademe olarak 10px aşağıdan yerine oturur ve solar içeri; her
 * biri bir öncekinden 70ms sonra başlar. [LaunchedEffect] her zaman
 * [Unit] anahtarıyla çalışır, bu yüzden bu composable yeniden
 * composition'a girdiğinde (profil sekmesi her açıldığında) animasyon
 * baştan oynar.
 */
@Composable
private fun StaggeredSection(
    index: Int,
    modifier: Modifier = Modifier,
    staggerMs: Long = 70L,
    durationMillis: Int = 340,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val startOffsetPx = with(density) { 10.dp.toPx() }
    val alpha = remember { Animatable(0f) }
    val translationY = remember { Animatable(startOffsetPx) }

    LaunchedEffect(Unit) {
        delay(index * staggerMs)
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
            )
        }
        launch {
            translationY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha.value
            this.translationY = translationY.value
        }
    ) {
        content()
    }
}

/**
 * [saveTrigger] is bumped once per successful Kaydet tap; when it changes and
 * this row's own value actually differs from what it was before that save,
 * the row runs a highlight sequence — a lime tint + a 3px left accent line,
 * held for 900ms and faded via [animateColorAsState] — after a
 * `highlightIndex * 80ms` delay so rows that changed cascade in order rather
 * than lighting up all at once. Rows that never change through the edit
 * sheet (e.g. Cinsiyet) simply leave [saveTrigger] at its default and never
 * highlight.
 */
@Composable
private fun InfoRow(
    label: String,
    rawValue: String,
    unit: String?,
    onClick: () -> Unit,
    isPercent: Boolean = false,
    showDivider: Boolean = true,
    saveTrigger: Int = 0,
    highlightIndex: Int = 0
) {
    val isEmpty = rawValue.isBlank() || rawValue == "-"
    val displayValue = if (isEmpty) "—" else if (isPercent) "%$rawValue" else rawValue

    var previousDisplay by remember { mutableStateOf(displayValue) }
    var isHighlighted by remember { mutableStateOf(false) }
    LaunchedEffect(saveTrigger) {
        if (saveTrigger > 0 && displayValue != previousDisplay) {
            delay(highlightIndex * 80L)
            isHighlighted = true
            delay(900)
            isHighlighted = false
        }
        previousDisplay = displayValue
    }

    val highlightColor by animateColorAsState(
        targetValue = if (isHighlighted) MD.Lime else Color.Transparent,
        animationSpec = tween(500)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .then(if (showDivider) Modifier.topRule(1.dp, MD.BorderWeak) else Modifier)
            .background(highlightColor.copy(alpha = highlightColor.alpha * 0.12f))
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(highlightColor)
        )
        Spacer(modifier = Modifier.width(11.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MD.TextMuted,
            modifier = Modifier.weight(1f)
        )
        AnimatedContent(
            targetState = displayValue,
            transitionSpec = {
                (slideInVertically(tween(160)) { h -> h / 2 } + fadeIn(tween(160))) togetherWith
                    (slideOutVertically(tween(160)) { h -> -h / 2 } + fadeOut(tween(160)))
            },
            label = "infoRowValue"
        ) { animatedDisplay ->
            if (animatedDisplay == "—") {
                Text(
                    text = animatedDisplay,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.TextFaint
                )
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = animatedDisplay,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MD.TextPrimary
                    )
                    if (unit != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MD.TextDim
                        )
                    }
                }
            }
        }
    }
}

/**
 * Edit Profile Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val editableValues = viewModel.getEditableValues()

    var name by remember { mutableStateOf(editableValues.name) }
    var height by remember { mutableStateOf(editableValues.height) }
    var weight by remember { mutableStateOf(editableValues.weight) }
    var fat by remember { mutableStateOf(editableValues.fat) }
    var age by remember { mutableStateOf(editableValues.age) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MD.Bg,
        scrimColor = Color(0xBF000000),
        shape = RoundedCornerShape(0.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(2.dp)
                    .background(MD.BorderStrong)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profili Düzenle",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.TextPrimary
                )
                Text(
                    text = "İPTAL",
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.TextDim,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StaggeredSection(index = 0, staggerMs = 50L, durationMillis = 300) {
                    CompactInputField(label = "İsim", value = name, onValueChange = { name = it })
                }
                StaggeredSection(index = 1, staggerMs = 50L, durationMillis = 300) {
                    CompactInputField(label = "Boy (cm)", value = height, onValueChange = { height = it })
                }
                StaggeredSection(index = 2, staggerMs = 50L, durationMillis = 300) {
                    CompactInputField(label = "Kilo (kg)", value = weight, onValueChange = { weight = it })
                }
                StaggeredSection(index = 3, staggerMs = 50L, durationMillis = 300) {
                    CompactInputField(label = "Yağ Oranı (%)", value = fat, onValueChange = { fat = it })
                }
                StaggeredSection(index = 4, staggerMs = 50L, durationMillis = 300) {
                    CompactInputField(label = "Yaş", value = age, onValueChange = { age = it })
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp, bottom = 20.dp)
                    .border(2.dp, MD.Lime)
                    .bouncyClickable(onClick = {
                        viewModel.saveProfile(
                            name = name,
                            heightStr = height,
                            weightStr = weight,
                            fatStr = fat,
                            ageStr = age,
                            gender = ""
                        )
                        onSaved()
                        onDismiss()
                    })
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "KAYDET",
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MD.Lime
                )
            }
        }
    }
}

/**
 * Kameranın açtığı küçük seçim sayfası — "DÜZENLE" sayfasıyla birebir aynı
 * [ModalBottomSheet] yapılandırmasını (containerColor/scrimColor/shape/
 * dragHandle) kullanır, böylece aynı eğriyle açılır/kapanır. Sistemin kendi
 * galeri/kamera arayüzü bu sayfanın dışında kalır — onların açılışını
 * yeniden zamanlayamayız, ama bu seçim adımını kontrol edebiliriz.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvatarChooserDialog(
    onDismiss: () -> Unit,
    onPickGallery: () -> Unit,
    onPickCamera: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MD.Bg,
        scrimColor = Color(0xBF000000),
        shape = RoundedCornerShape(0.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(2.dp)
                    .background(MD.BorderStrong)
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 4.dp)) {
            Text(
                text = "Profil Fotoğrafı",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MD.TextPrimary,
                modifier = Modifier.padding(bottom = 14.dp)
            )
            AvatarChooserRow(
                icon = Icons.Default.PhotoLibrary,
                label = "Galeriden Seç",
                onClick = onPickGallery
            )
            AvatarChooserRow(
                icon = Icons.Default.CameraAlt,
                label = "Kamerayla Çek",
                onClick = onPickCamera,
                showDivider = true
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AvatarChooserRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    showDivider: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (showDivider) Modifier.topRule(1.dp, MD.BorderWeak) else Modifier)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MD.Lime, modifier = Modifier.size(20.dp))
        Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MD.TextPrimary)
    }
}

/**
 * Tam ekran kamera yakalama — PeekabooCamera sadece önizleme yüzeyini verir,
 * kepenk ve kapat düğmelerini Modernist Dark'a uygun şekilde biz çiziyoruz.
 */
@Composable
private fun AvatarCameraCapture(
    onCapture: (ByteArray?) -> Unit,
    onClose: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        PeekabooCamera(
            modifier = Modifier.fillMaxSize(),
            captureIcon = { onClick ->
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .border(3.dp, MD.Lime)
                        .padding(6.dp)
                        .background(MD.Lime)
                        .bouncyClickable(pressedScale = 0.85f, onClick = onClick)
                )
            },
            onCapture = onCapture
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(16.dp)
                .size(36.dp)
                .background(Color(0x99000000))
                .bouncyClickable(pressedScale = 0.82f, onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Kapat",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CompactInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label.uppercase(),
            fontSize = 9.sp,
            letterSpacing = 1.2.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MD.TextMuted
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = MD.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            cursorBrush = SolidColor(MD.Lime),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, MD.BorderStrong)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

/**
 * Custom modifier to add an iOS-like bouncy scale down animation on press.
 * The press-in step uses a quick tween rather than the release spring — on
 * a fast tap whose onClick immediately opens a sheet/dialog, a symmetric
 * spring both ways barely moves before it's covered by the new screen, so
 * the shrink read as "no effect at all". A near-instant press-in means the
 * shrink is visible before that happens; the bouncy spring is kept for the
 * release recoil, which still has the screen to itself for a moment.
 *
 * [pressedScale] defaults to 0.94, which reads fine on a full-width button
 * but is a ~1-2dp shift on a small icon-only target (e.g. the 26dp camera
 * button) — imperceptible regardless of timing. Small targets should pass a
 * lower value (e.g. 0.82) for the same shrink to actually be visible; this
 * still is the same modifier/mechanism everywhere, just tuned per call site.
 */
fun Modifier.bouncyClickable(
    pressedScale: Float = 0.94f,
    onClick: () -> Unit
) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = if (isPressed) {
            tween(durationMillis = 60)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        },
        label = "bouncyScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}
