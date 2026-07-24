package com.example.afyagpt.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.afyagpt.ui.components.*
import com.example.afyagpt.ui.navigation.AppRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Handle showing protocol details in a BottomSheet or Dialog.
    // For simplicity, we use a ModalBottomSheet if a protocol is selected.
    if (state.selectedProtocol != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectProtocol(null) }
        ) {
            ProtocolDetailSheet(protocol = state.selectedProtocol!!)
        }
    }

    Scaffold(
        topBar = {
            AfyaTopBar(title = "Clinical Library")
        },
        bottomBar = {
            AfyaBottomNav(
                currentRoute = AppRoute.Library.route,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AfyaSearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.search(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = "Search conditions, symptoms..."
            )

            if (state.filteredProtocols.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AfyaEmptyState(
                        icon = Icons.Default.MenuBook,
                        title = "No Protocols Found",
                        subtitle = "Try adjusting your search query."
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.filteredProtocols) { protocol ->
                        ProtocolCard(
                            protocol = protocol,
                            onClick = { viewModel.selectProtocol(protocol) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProtocolCard(protocol: Protocol, onClick: () -> Unit) {
    AfyaCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = protocol.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = protocol.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProtocolDetailSheet(protocol: Protocol) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = protocol.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = protocol.category,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = protocol.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (protocol.dangerSigns.isNotEmpty()) {
            item {
                Text("Danger Signs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                protocol.dangerSigns.forEach { sign ->
                    Text("• $sign", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Text("Symptoms to Look For", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            protocol.symptoms.forEach { sym ->
                Text("• $sym", style = MaterialTheme.typography.bodyMedium)
            }
        }

        item {
            Text("Treatment Steps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            protocol.treatmentSteps.forEachIndexed { index, step ->
                Text("${index + 1}. $step", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
