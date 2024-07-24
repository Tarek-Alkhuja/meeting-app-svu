// Getting elements from DOM
const joinButton = document.getElementById("joinBtn");
const leaveButton = document.getElementById("leaveBtn");
const toggleMicButton = document.getElementById("toggleMicBtn");
const toggleWebCamButton = document.getElementById("toggleWebCamBtn");
const createButton = document.getElementById("createMeetingBtn");
const joinScreen = document.getElementById("join-screen");
const videoContainer = document.getElementById("videoContainer");
const participantList = document.getElementById("participantList");
const chatBox = document.getElementById("chatBox");
const chatInput = document.getElementById("chatInput");
const sendMsgButton = document.getElementById("sendMsgBtn");
const chatMessages = document.getElementById("chatMessages");
const meetingIdHeading = document.getElementById("meetingIdHeading");
const endMeetingButton = document.getElementById("endMeetingBtn");
const chatWrapper = document.getElementById("chatWrapper");
const chatCloseBtn = document.getElementById("chatCloseBtn");
const chatOpenBtn = document.getElementById("chatOpenBtn");
let btnScreenShare = document.getElementById("btnScreenShare");
let videoScreenShare = document.getElementById("videoScreenShare");
const creatorControls = document.getElementById("creator-controls");


let meeting = null;
let meetingId = "";
let isMicOn = false;
let isWebCamOn = false;
let isRecording = false;
let isCreator = false;
let userEmail = "";
let screenShareOn = false;

const displayMediaOptions = {
    video: {
        cursor: "always",
        height: 1000,
        width: 1200
    },
    audio: false
};


// Hide join-screen div
function hideJoinScreen() {
    joinScreen.style.display = "none";
}

// Join Meeting Button Event Listener
joinButton.addEventListener("click", async () => {
    userEmail = document.getElementById("emailInput").value;
    const roomId = document.getElementById("meetingIdTxt").value;
    // Validate email and meeting ID
    if (!userEmail) {
        alert("Email is required to join a meeting.");
        return;
    }
    if (!roomId) {
        alert("Meeting ID is required to join a meeting.");
        return;
    }
    meetingId = roomId;

    await initializeMeeting(userEmail);
    hideJoinScreen();
});

// Create Meeting Button Event Listener
createButton.addEventListener("click", async () => {
    isCreator = true;
    userEmail = document.getElementById("emailInput").value;
    // Validate email
    if (!userEmail) {
        alert("Email is required to create a meeting.");
        return;
    }
    const meetingIdField = document.getElementById("meetingIdTxt").value;
    if (meetingIdField) {
        alert("This field should be empty when creating a meeting.");
        return;
    }
    const url = `https://api.videosdk.live/v2/rooms`;
    const options = {
        method: "POST",
        headers: { Authorization: TOKEN, "Content-Type": "application/json" },
    };

    const { roomId } = await fetch(url, options)
        .then((response) => response.json())
        .catch((error) => alert("error", error));
    meetingId = roomId;

    await initializeMeeting(userEmail);
    hideJoinScreen();
});

// Initialize meeting
async function initializeMeeting(email) {
    window.VideoSDK.config(TOKEN);

    meeting = await VideoSDK.initMeeting({
        meetingId: meetingId,
        name: email,
        micEnabled: true,
        webcamEnabled: true,
    });

    meeting.join();

    // creating local participant
    createLocalParticipant();

    meeting.on("meeting-joined", () => {
        document.getElementById("grid-screen").style.display = "block";
        if (isCreator) {
            meetingIdHeading.textContent = `Meeting ID: ${meetingId}`;
            meetingIdHeading.style.display = "block";
            endMeetingButton.style.display = "block";
            creatorControls.style.display = "block";
        }
    });

    meeting.on("meeting-left", () => {
        videoContainer.innerHTML = "";
        participantList.innerHTML = "";
        chatMessages.innerHTML = "";
    });

    // participant joined
    meeting.on("participant-joined", (participant) => {
        addParticipant(participant.id, participant.displayName);
        participant.on("stream-enabled", (stream) => {
            setTrack(stream, participant, false);
        });
    });

    // participants left
    meeting.on("participant-left", (participant) => {
        removeParticipant(participant.id);
    });

    // Receive chat message
    meeting.on("chat-message", (data) => {
        if (data && data.text) {
            const messageData = JSON.parse(data.text);
            if (messageData.senderName !== userEmail) {
                displayChatMessage(messageData.senderName, messageData.message, new Date(messageData.timestamp));
            }
        }
    });

    // Subscribe to chat messages
    meeting.pubSub.subscribe("CHAT", (data) => {
        const { message, senderName, timestamp } = JSON.parse(data.message);
        displayChatMessage(senderName, message, new Date(timestamp));
    });
  meeting.on("presenter-changed", (presenterId) => {
    if (presenterId) {
      console.log(presenterId);
      //videoScreenShare.style.display = "inline-block";
    } else {
      console.log(presenterId);
      videoScreenShare.removeAttribute("src");
      videoScreenShare.pause();
      videoScreenShare.load();
      videoScreenShare.style.display = "none";

      btnScreenShare.style.color = "white";
      screenShareOn = false;
      console.log(`screen share on : ${screenShareOn}`);
    }
  });
}

function createLocalParticipant() {
    addParticipant(meeting.localParticipant.id, "You");
    meeting.localParticipant.on("stream-enabled", (stream) => {
        setTrack(stream, meeting.localParticipant, true);
    });
}

function addParticipant(id, name) {
    const participantItem = document.createElement("div");
    participantItem.setAttribute("id", `p-${id}`);
    participantItem.textContent = name;
    participantList.appendChild(participantItem);

    const videoElement = createVideoElement(id);
    const audioElement = createAudioElement(id);

    videoContainer.appendChild(videoElement);
    videoContainer.appendChild(audioElement);
}

function removeParticipant(id) {
    const participantItem = document.getElementById(`p-${id}`);
    if (participantItem) participantItem.remove();

    const videoElement = document.getElementById(`v-${id}`);
    if (videoElement) videoElement.remove();

    const audioElement = document.getElementById(`a-${id}`);
    if (audioElement) audioElement.remove();
}

// Display chat message
function displayChatMessage(sender, message, timestamp) {
    const messageItem = document.createElement("div");
    messageItem.innerHTML = `
        <div><strong>${sender}</strong> <span style="font-size: small; color: grey;">${timestamp.toLocaleTimeString()}</span></div>
        <div>${message}</div>`;
    chatMessages.appendChild(messageItem);
    chatMessages.scrollTop = chatMessages.scrollHeight; // Scroll to bottom
}

// Send message button event listener
sendMsgButton.addEventListener("click", sendMessage);

// Send message on Enter key press
chatInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter") {
        sendMessage();
    }
})

function sendMessage() {
    const message = chatInput.value;
    if (message.trim() !== "") {
        const messageData = {
            senderName: userEmail, // Use the user's email as the sender name
            message: message,
            timestamp: Date.now(),
            isSender: true // Flag to indicate this message is from the sender
        };
        const messageString = JSON.stringify(messageData);
        meeting.sendChatMessage(messageString);
        // Directly display the message for the sender
        displayChatMessage("You", message, new Date(messageData.timestamp));
        chatInput.value = "";
    }
}

leaveButton.addEventListener("click", async () => {
    meeting.leave();
    window.location.reload();
});

endMeetingButton.addEventListener("click", async () => {
    meeting.pubSub.publish("END_MEETING", {});
    meeting.end();
    window.location.reload();
});

toggleMicButton.addEventListener("click", () => {
    if (isMicOn) {
        meeting.muteMic();
    } else {
        meeting.unmuteMic();
    }
    isMicOn = !isMicOn;
});

toggleWebCamButton.addEventListener("click", () => {
    if (isWebCamOn) {
        meeting.disableWebcam();
        document.getElementById(`f-${meeting.localParticipant.id}`).style.display = "none";
    } else {
        meeting.enableWebcam();
        document.getElementById(`f-${meeting.localParticipant.id}`).style.display = "inline";
    }
    isWebCamOn = !isWebCamOn;
});

// Function to set media track
function setTrack(stream, participant, isLocal) {
    if (stream.kind === "video") {
        isWebCamOn = true;
        const mediaStream = new MediaStream();
        mediaStream.addTrack(stream.track);
        const videoElm = document.getElementById(`v-${participant.id}`);
        if (videoElm) {
            videoElm.srcObject = mediaStream;
            videoElm.play().catch((error) => console.error("videoElm.play() failed", error));
        }
    }
    if (stream.kind === "audio") {
        if (isLocal) {
            isMicOn = true;
        } else {
            const mediaStream = new MediaStream();
            mediaStream.addTrack(stream.track);
            const audioElement = document.getElementById(`a-${participant.id}`);
            if (audioElement) {
                audioElement.srcObject = mediaStream;
                audioElement.play().catch((error) => console.error("audioElem.play() failed", error));
            }
        }
    }

      if (stream.kind == "share" && !isLocal) {
        screenShareOn = true;
        const mediaStream = new MediaStream();
        mediaStream.addTrack(stream.track);
        videoScreenShare.srcObject = mediaStream;
        videoScreenShare
          .play()
          .catch((error) =>
            console.error("videoElem.current.play() failed", error)
          );
        videoScreenShare.style.display = "inline-block";
        btnScreenShare.style.color = "grey";
      }
}

function createVideoElement(pId) {
    const videoFrame = document.createElement("div");
    videoFrame.setAttribute("id", `f-${pId}`);

    const videoElement = document.createElement("video");
    videoElement.classList.add("video-frame");
    videoElement.setAttribute("id", `v-${pId}`);
    videoElement.setAttribute("playsinline", true);
    videoElement.setAttribute("width", "300");
    videoFrame.appendChild(videoElement);

    return videoFrame;
}

function createAudioElement(pId) {
    const audioElement = document.createElement("audio");
    audioElement.setAttribute("autoPlay", "false");
    audioElement.setAttribute("playsInline", "true");
    audioElement.setAttribute("controls", "false");
    audioElement.setAttribute("id", `a-${pId}`);
    audioElement.style.display = "none";
    return audioElement;
}

// Toggle chat window
chatOpenBtn.addEventListener("click", () => {
    chatWrapper.style.display = "block";
    chatOpenBtn.style.display = "none";
});

chatCloseBtn.addEventListener("click", () => {
    chatWrapper.style.display = "none";
    chatOpenBtn.style.display = "block";
});

// Request necessary permissions
async function requestPermissions() {
    try {
        const audioPermission = await window.VideoSDK.requestPermission(window.VideoSDK.Constants.permission.AUDIO);
        const videoPermission = await window.VideoSDK.requestPermission(window.VideoSDK.Constants.permission.VIDEO);
        const audiovideoPermission = await window.VideoSDK.requestPermission(window.VideoSDK.Constants.permission.AUDIO_AND_VIDEO);

        console.log("Audio Permission:", audioPermission);
        console.log("Video Permission:", videoPermission);
        console.log("Audio and Video Permission:", audiovideoPermission);
    } catch (e) {
        console.error("Permission request failed", e.message);
    }
}
// Initialize permissions on page load
window.addEventListener("load", requestPermissions);

var RECORDING_ONGOING = false;
var recordingToggle = document.getElementById("recording-toggle");
var qualitySelect = document.getElementById("quality");

recordingToggle.addEventListener("click", function() {
    RECORDING_ONGOING = !RECORDING_ONGOING;
    if (RECORDING_ONGOING) {
        recordingToggle.innerHTML = "Stop Recording";
        startRecording();
    } else {
        recordingToggle.innerHTML = "Start Recording";
        stopRecording();
    }
});

var blob, mediaRecorder = null;
var chunks = [];

async function startRecording() {
    var quality = qualitySelect.value;
    var constraints = {
        video: {
            mediaSource: "screen",
            width: quality == "720" ? 1280 : (quality == "480" ? 854 : 640),
            height: quality == "720" ? 720 : (quality == "480" ? 480 : 360)
        },
        audio: true
    };

    var stream = await navigator.mediaDevices.getDisplayMedia(constraints);
    mediaRecorder = new MediaRecorder(stream, { mimeType: "video/webm" });

    mediaRecorder.ondataavailable = (e) => {
        if (e.data.size > 0) {
            chunks.push(e.data);
        }
    };

    mediaRecorder.onstop = () => {
        var filename = window.prompt("File name", "video");

        blob = new Blob(chunks, { type: "video/webm" });
        chunks = [];
        var dataDownloadUrl = URL.createObjectURL(blob);

        let a = document.createElement('a');
        a.href = dataDownloadUrl;
        a.download = `${filename}.webm`;
        a.click();

        URL.revokeObjectURL(dataDownloadUrl);
    };

    mediaRecorder.start(250);
}

function stopRecording() {
    mediaRecorder.stop();
}


  // screen share button event listener
  btnScreenShare.addEventListener("click", async () => {
    if (btnScreenShare.style.color == "grey") {
      meeting.disableScreenShare();
    } else {
      meeting.enableScreenShare();
    }
  });





