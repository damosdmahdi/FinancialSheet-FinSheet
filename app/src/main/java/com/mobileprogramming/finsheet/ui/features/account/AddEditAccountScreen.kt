package com.mobileprogramming.finsheet.ui.features.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mobileprogramming.finsheet.ui.features.addtransaction.CategoryIconMapper
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private data class IconOption(
    val iconName: String,
    val icon: ImageVector
)

private val availableIcons = listOf(
    IconOption("AccountBalanceWallet", Icons.Outlined.AccountBalanceWallet),
    IconOption("Savings", Icons.Outlined.Savings),
    IconOption("CreditCard", Icons.Outlined.CreditCard),
    IconOption("Payments", Icons.Outlined.Payments),
    IconOption("MonetizationOn", Icons.Outlined.MonetizationOn),
    IconOption("LocalAtm", Icons.Outlined.LocalAtm),
    IconOption("AccountBalance", Icons.Outlined.AccountBalance),
    IconOption("TrendingUp", Icons.Outlined.TrendingUp),
    IconOption("PriceChange", Icons.Outlined.PriceChange),
    IconOption("AttachMoney", Icons.Outlined.AttachMoney),
    IconOption("Paid", Icons.Outlined.Paid)
)

private val availableColors = listOf(
    "1A3DA8", "2DC653", "FF8C00", "E53935", "8E24AA", "E91E8C", "00ACC1"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountScreen(
    viewModel: AccountViewModel,
    accountId: String?,
    onNavigateBack: () -> Unit
) {
    val name by viewModel.name.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val selectedIcon by viewModel.selectedIcon.collectAsState()
    val selectedColor by viewModel.selectedColor.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    val isEditMode = accountId != null
    val primaryBlue = Color(0xFF1A5BEB)

    LaunchedEffect(accountId) {
        viewModel.initForm(accountId)
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onNavigateBack()
        }
    }

    if (isEditMode && name.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = primaryBlue)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Ubah Rekening" else "Tambah Rekening", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AccountBottomBar(
                onCancel = onNavigateBack,
                onSave = { viewModel.saveAccount() },
                primaryBlue = primaryBlue,
                isEditMode = isEditMode,
                saveEnabled = name.isNotBlank()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 1. Nama Rekening
            FormFieldSection(
                label = "Nama Rekening",
                value = name,
                onValueChange = viewModel::onNameChange,
                placeholderText = "Cth: Bank Mandiri, Dompet Utama",
                primaryBlue = primaryBlue
            )

            // 2. Saldo
            FormFieldSection(
                label = if (isEditMode) "Saldo" else "Saldo Awal",
                value = balance,
                onValueChange = viewModel::onBalanceChange,
                placeholderText = "0",
                primaryBlue = primaryBlue,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = RupiahVisualTransformation(),
                leadingIcon = {
                    Text(
                        text = "Rp",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            // 3. Pilih Ikon (Layout 4 Kolom mirip seperti kategori)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Pilih Ikon",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                IconPickerGrid(
                    icons = availableIcons,
                    selectedIconName = selectedIcon,
                    selectedColorHex = selectedColor,
                    onIconSelected = { viewModel.onIconSelected(it) }
                )
            }

            // 4. Pilih Warna (Layout Lingkaran dengan Checkmark mirip seperti kategori)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Pilih Warna",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                ColorPickerGrid(
                    colors = availableColors,
                    selectedColorHex = selectedColor,
                    onColorSelected = { viewModel.onColorSelected(it) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun FormFieldSection(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    primaryBlue: Color,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholderText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            leadingIcon = leadingIcon,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
private fun IconPickerGrid(
    icons: List<IconOption>,
    selectedIconName: String,
    selectedColorHex: String,
    onIconSelected: (String) -> Unit
) {
    val chunked = icons.chunked(4)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { option ->
                    Box(modifier = Modifier.weight(1f)) {
                        IconGridItem(
                            option = option,
                            isSelected = option.iconName == selectedIconName,
                            selectedColorHex = selectedColorHex,
                            onClick = { onIconSelected(option.iconName) }
                        )
                    }
                }
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun IconGridItem(
    option: IconOption,
    isSelected: Boolean,
    selectedColorHex: String,
    onClick: () -> Unit
) {
    val selectedColor = CategoryIconMapper.getColorByHex(selectedColorHex)
    val borderColor = if (isSelected) selectedColor
                      else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val bgColor = if (isSelected) selectedColor.copy(alpha = 0.08f)
                  else MaterialTheme.colorScheme.surface
    val iconTint = if (isSelected) selectedColor
                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = iconTint
        )
    }
}

@Composable
private fun ColorPickerGrid(
    colors: List<String>,
    selectedColorHex: String,
    onColorSelected: (String) -> Unit
) {
    val chunked = colors.chunked(5)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        chunked.forEach { rowColors ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowColors.forEach { colorHex ->
                    ColorSwatch(
                        colorHex = colorHex,
                        isSelected = colorHex == selectedColorHex,
                        onClick = { onColorSelected(colorHex) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = CategoryIconMapper.getColorByHex(colorHex)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Dipilih",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun AccountBottomBar(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    primaryBlue: Color,
    isEditMode: Boolean,
    saveEnabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, primaryBlue),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = primaryBlue
                )
            ) {
                Text("Batal", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onSave,
                enabled = saveEnabled,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isEditMode) "Simpan Perubahan" else "Simpan Rekening",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

class RupiahVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }
        
        val formattedText = try {
            val number = originalText.toLong()
            val formatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.Builder().setLanguage("id").setRegion("ID").build()))
            formatter.format(number).replace(',', '.')
        } catch (e: Exception) {
            originalText
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (originalText.isEmpty()) return 0
                var digitsCount = 0
                for (i in 0 until formattedText.length) {
                    if (digitsCount == offset) return i
                    if (formattedText[i].isDigit()) {
                        digitsCount++
                    }
                }
                return formattedText.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (formattedText.isEmpty()) return 0
                var digitsCount = 0
                for (i in 0 until minOf(offset, formattedText.length)) {
                    if (formattedText[i].isDigit()) {
                        digitsCount++
                    }
                }
                return digitsCount
            }
        }
        
        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
