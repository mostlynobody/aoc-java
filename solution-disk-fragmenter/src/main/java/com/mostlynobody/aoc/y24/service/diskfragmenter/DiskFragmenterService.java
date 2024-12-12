package com.mostlynobody.aoc.y24.service.diskfragmenter;

import com.mostlynobody.aoc.y24.shared.records.SolutionJson;
import com.mostlynobody.aoc.y24.shared.service.SolutionService;
import com.mostlynobody.aoc.y24.shared.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class DiskFragmenterService implements SolutionService {


    @Override
    public SolutionJson solve(String rawInput) {
        char[] disk = Utils.removeLastLineBreak(rawInput).toCharArray();

        // PART 1
        Filesystem fs = new Filesystem(disk);
        fs.removeTrailingFreeSpace();
        int head = 0;
        while (head < fs.blocks.size()) {
            if (fs.blocks.get(head) != -1) {
                head++;
                continue;
            }

            fs.removeTrailingFreeSpace();
            fs.blocks.set(head, fs.blocks.removeLast());
        }

        long silver = fs.calculateChecksum();

        // PART 2
        fs = new Filesystem(disk);
        fs.removeTrailingFreeSpace();
        int fileId = fs.blocks.getLast();
        while (fileId >= 0) {
            int fileSize = fs.getFileSize(fileId);
            int fileHead = fs.getFirstBlockOfId(fileId);
            int spaceHead = fs.findSpace(fileSize, fileHead);

            if (spaceHead != -1) {
                fs.swapSpace(spaceHead, fileHead, fileSize);
                fs.removeTrailingFreeSpace();
            }
            fileId--;
        }

        long gold = fs.calculateChecksum();

        return new SolutionJson(String.valueOf(silver), String.valueOf(gold));
    }

    private static class Filesystem {
        char[] input;
        List<Integer> blocks = new ArrayList<>();

        public Filesystem(char[] input) {
            this.input = input;
            for (int i = 0; i < input.length; i++) {
                int id = i % 2 == 0 ? i / 2 : -1;
                for (int n = input[i] - '0'; n > 0; n--) blocks.add(id);
            }
        }

        private int getFileSize(int id) {
            return input[id * 2] - '0';
        }


        private int getFirstBlockOfId(int id) {
            for (int i = 0; i < blocks.size(); i++) {
                if (blocks.get(i) == id) return i;
            }
            return -1;
        }


        private int findSpace(int size, int upperBound) {
            int head = 0;
            while (head + size - 1 < blocks.size() && head + size - 1 < upperBound) {
                if (blocks.get(head) != -1) {
                    head++;
                    continue;
                }

                boolean isContinuous = true;
                for (int i = head; i < head + size; i++) {
                    if (blocks.get(i) != -1) {
                        isContinuous = false;
                        head = i + 1;
                        break;
                    }
                }

                if (isContinuous) {
                    return head;
                }
            }

            return -1;
        }

        private void removeTrailingFreeSpace() {
            while (blocks.getLast() == -1) blocks.removeLast();
        }

        private void swapSpace(int head1, int head2, int size) {
            for (int i = 0; i < size; i++) {
                int tmp = blocks.get(head1 + i);
                blocks.set(head1 + i, blocks.get(head2 + i));
                blocks.set(head2 + i, tmp);
            }
        }

        private long calculateChecksum() {
            long sum = 0;
            for (int i = 0; i < blocks.size(); i++) {
                var block = blocks.get(i);
                if (block != -1) sum += (long) i * blocks.get(i);
            }
            return sum;
        }
    }
}
