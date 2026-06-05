package com.novaos.launcher.ui.home.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.novaos.launcher.domain.model.AppInfo
import com.novaos.launcher.domain.model.FolderInfo
import com.novaos.launcher.domain.model.IconShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderView(
    folderInfo: FolderInfo,
    apps: List<AppInfo>,
    iconShape: IconShape = IconShape.SQUIRCLE,
    isDarkTheme: Boolean,
    isEditMode: Boolean,
    onRename: (String) -> Unit,
    onAppTap: (AppInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var folderName by remember(folderInfo.name) { mutableStateOf(folderInfo.name) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDarkTheme) Color.Black.copy(alpha = 0.82f)
                    else Color.White.copy(alpha = 0.85f)
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = { onDismiss() },
                        onDrag = { _, dragAmount ->
                            if (dragAmount.y > 40) onDismiss()
                        }
                    )
                }
                .systemBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.75f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Folder Name input/label
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditMode) {
                        BasicTextField(
                            value = folderName,
                            onValueChange = {
                                folderName = it
                                onRename(it)
                            },
                            textStyle = TextStyle(
                                color = if (isDarkTheme) Color.White else Color.Black,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                    } else {
                        Text(
                            text = folderName,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Beautiful Frosted Card for grid of apps
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            if (isDarkTheme) Color.White.copy(alpha = 0.08f)
                            else Color.Black.copy(alpha = 0.05f)
                        )
                        .padding(24.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(apps) { app ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onAppTap(app) }
                            ) {
                                AppIcon(
                                    label = "",
                                    icon = app.icon,
                                    iconShape = iconShape,
                                    iconSize = 56f,
                                    showLabel = false,
                                    isEditMode = false,
                                    onTap = { onAppTap(app) }
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = app.displayLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDarkTheme) Color.White.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.85f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Dismiss button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.06f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = if (isDarkTheme) Color.White else Color.Black
                    )
                }
            }
        }
    }
}
