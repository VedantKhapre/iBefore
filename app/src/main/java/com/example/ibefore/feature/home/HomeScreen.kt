package com.example.ibefore.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ibefore.ui.theme.DarkGrey
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val channels = viewModel.channels.collectAsState()
    var addChannel by remember {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState()

    val primaryColor = Color(0xFF4285F4)
    val secondaryColor = Color(0xFF34A853)
    val backgroundColor = Color(0xFF121212)
    val surfaceColor = Color(0xFF1E1E1E)

    Scaffold(
        containerColor = backgroundColor,
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { addChannel = true },
                    containerColor = primaryColor,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Channel") },
                    text = { Text("Add Channel") }
                )

                ExtendedFloatingActionButton(
                    onClick = { navController.navigate("translate") },
                    containerColor = secondaryColor,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Translate, contentDescription = "Translate") },
                    text = { Text("Translate") }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            LazyColumn {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically // Aligns the elements vertically
                    ) {
                        Text(
                            text = "Messages",
                            color = Color.Gray,
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("login")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Logout",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }


                    }
                }


                item {
                    TextField(value = "",
                        onValueChange = {},
                        placeholder = { Text(text = "Search...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(
                                RoundedCornerShape(40.dp)
                            ),
                        textStyle = TextStyle(color = Color.LightGray),
                        colors = TextFieldDefaults.colors().copy(
                            focusedContainerColor = DarkGrey,
                            unfocusedContainerColor = DarkGrey,
                            focusedTextColor = Color.Gray,
                            unfocusedTextColor = Color.Gray,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray,
                            focusedIndicatorColor = Color.Gray
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search, contentDescription = null
                            )
                        })
                }

                items(channels.value) { channel ->
                    Column {
                        ChannelItem(
                            channelName = channel.name,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                            onClick = {
                                navController.navigate("chat/${channel.id}&${channel.name}")
                            }
                        )
                    }
                }
            }
        }
    }

    if (addChannel) {
        ModalBottomSheet(
            onDismissRequest = { addChannel = false },
            sheetState = sheetState,
            containerColor = surfaceColor
        ) {
            AddChannelDialog(
                onAddChannel = {
                    viewModel.addChannel(it)
                    addChannel = false
                }
            )
        }
    }
}



@Composable
fun ChannelItem(
    channelName: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkGrey)
            .clickable {
                onClick()
            }
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.Yellow.copy(alpha = 0.3f))

            ) {
                Text(
                    text = channelName[0].uppercase(),
                    color = Color.White,
                    style = TextStyle(fontSize = 35.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }


            Text(text = channelName, modifier = Modifier.padding(8.dp), color = Color.White)
        }
    }
}

@Composable
fun AddChannelDialog(onAddChannel: (String) -> Unit) {
    var channelName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create New Channel",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        TextField(
            value = channelName,
            onValueChange = { channelName = it },
            label = { Text("Channel Name", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.Gray,
                focusedContainerColor = Color(0xFF2C2C2C),
                unfocusedContainerColor = Color(0xFF2C2C2C)
            )
        )

        Button(
            onClick = {
                if (channelName.isNotBlank()) {
                    onAddChannel(channelName)
                    channelName = ""
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4285F4)
            ),
            enabled = channelName.isNotBlank()
        ) {
            Text("Create Channel", color = Color.White)
        }
    }
}